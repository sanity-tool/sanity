#!/bin/bash

# Exit on failure
set -e

LLVM_CONFIG=llvm-config
$LLVM_CONFIG --version >/dev/null 2>&1 || LLVM_CONFIG=/usr/local/opt/llvm/bin/llvm-config

LLVM_LIBS="irreader transformutils"

case `uname` in
    Linux)
        gcc -v || no gcc
        gcc-4.9 -v || no gcc

        CC=gcc-4.9
        CXX=g++-4.9
        LD=g++-4.9
        OBJCOPY=objcopy

        LLVM_CONFIG=llvm-config-3.8

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

        DLL_NAME=libirreader.so

        LIBS="`$LLVM_CONFIG --libfiles $LLVM_LIBS` -ltermcap"
        LD_FLAGS="-Wl,-z,defs"
    ;;
    Darwin)
        CC=clang
        CXX=clang++
        LD=clang++
        OBJCOPY=gobjcopy

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

        DLL_NAME=libirreader.jnilib

        LIBS="`$LLVM_CONFIG --libs $LLVM_LIBS` -ltermcap"
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

echo `$LLVM_CONFIG --version`

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS="`$LLVM_CONFIG --ldflags` -L/usr/local/opt/libffi/lib -L/usr/local/opt/llvm/lib -Wl,-rpath,/usr/local/opt/llvm/lib $LDFLAGS"

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"

DEBUG="-g -coverage"

SRC_DIR="src/main/cpp"

JAVA_OUT="target/generated-sources/java/na/okutane/cpp/llvm"
mkdir -p $JAVA_OUT

CPP_OUT="target/generated-sources/jni"
mkdir -p $CPP_OUT

OBJ_DIR="target/native/static"
mkdir -p $OBJ_DIR

SOBJ_DIR="target/native/shared"
mkdir -p $SOBJ_DIR

swig -cpperraswarn $LLVM_INCLUDE -java -outdir $JAVA_OUT -package na.okutane.cpp.llvm -o $CPP_OUT/bitreader_wrap.c -v $SRC_DIR/bitreader.i

$CC -c $CPP_OUT/bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE -I/usr/local/opt/llvm/include $DEBUG -fPIC -o $OBJ_DIR/wrappers.o

$CXX -c $SRC_DIR/helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $CPPFLAGS -I/usr/local/opt/llvm/include $DEBUG -fPIC -std=c++11 -o $OBJ_DIR/helpers.o

if [ -z "$REAL_LLVM" ]; then
    # llvm drops debug information (hence source refs) from some compilers, code below disabled that logic.

    # compile replacement code.
    $CXX -c $SRC_DIR/debughack.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $CPPFLAGS -I/usr/local/opt/llvm/include $DEBUG -fPIC -std=c++11 -o $OBJ_DIR/debughack.o

    # remove logic from llvm library.
    cp /usr/local/Cellar/llvm/3.8.1/lib/libLLVMCore.a $OBJ_DIR/libLLVMCore.a
    chmod +w $OBJ_DIR/libLLVMCore.a
    $OBJCOPY -v --strip-symbol __ZN4llvm16UpgradeDebugInfoERNS_6ModuleE $OBJ_DIR/libLLVMCore.a

    # link against "hacked" library and replacement code.
    LIBS="${LIBS/-lLLVMCore/} $OBJ_DIR/libLLVMCore.a $OBJ_DIR/debughack.o"

    LDFLAGS="-Wl,-allow_sub_type_mismatches ${LDFLAGS}"
fi

$CXX -shared $LIBS $OBJ_DIR/wrappers.o $OBJ_DIR/helpers.o -o $SOBJ_DIR/$DLL_NAME -L/usr/local/opt/libffi/lib $LDFLAGS $DEBUG