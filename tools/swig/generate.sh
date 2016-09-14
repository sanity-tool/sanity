#!/bin/sh

# Exit on failure
set -e

LLVM_CONFIG=llvm-config
$LLVM_CONFIG --version >/dev/null 2>&1 || LLVM_CONFIG=/usr/local/opt/llvm/bin/llvm-config

case `uname` in
    Darwin)
        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

        HACK_DLL_NAME=libdebughack.jnilib
        DLL_NAME=libirreader.jnilib
    ;;
    Linux)
        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

        DLL_NAME=libirreader.so
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS="`$LLVM_CONFIG --ldflags` -L/usr/local/opt/libffi/lib -L/usr/local/opt/llvm/lib -Wl,-rpath,/usr/local/opt/llvm/lib"

# todo reduce to only necessary libs.
LIBS="`$LLVM_CONFIG --libs` -ltermcap"

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"
LLVM_LIBS=

DEBUG=-g

JAVA_OUT="../../target/generated-sources/java/na/okutane/cpp/llvm"
rm -rf $JAVA_OUT || echo already removed

CPP_OUT="../../target/generated-sources/jni"
rm -rf $CPP_OUT || echo already removed

mkdir -p $JAVA_OUT
mkdir -p $CPP_OUT
swig $LLVM_INCLUDE -java -outdir $JAVA_OUT -package na.okutane.cpp.llvm -o $CPP_OUT/bitreader_wrap.c -v bitreader.i

OBJ_DIR="../../target/native/static"
SOBJ_DIR="../../target/native/shared"
mkdir -p $OBJ_DIR
mkdir -p $SOBJ_DIR

clang -c $CPP_OUT/bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE -I/usr/local/opt/llvm/include $DEBUG -fPIC -o $OBJ_DIR/wrappers.o

clang++ -c helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $CPPFLAGS -I/usr/local/opt/llvm/include $DEBUG -fPIC -std=c++11 -o $OBJ_DIR/helpers.o

if [ -z "$REAL_LLVM" ]; then
    # llvm drops debug information (hence source refs) from some compilers, code below disabled that logic.

    # compile replacement code.
    clang++ -c debughack.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $CPPFLAGS -I/usr/local/opt/llvm/include $DEBUG -fPIC -std=c++11 -o $OBJ_DIR/debughack.o

    # remove logic from llvm library.
    cp /usr/local/Cellar/llvm/3.8.1/lib/libLLVMCore.a $OBJ_DIR/libLLVMCore.a
    chmod +w $OBJ_DIR/libLLVMCore.a
    gobjcopy -v --target mach-o-x86-64 --strip-symbol __ZN4llvm16UpgradeDebugInfoERNS_6ModuleE $OBJ_DIR/libLLVMCore.a

    # link against "hacked" library and replacement code.
    LIBS="${LIBS/-lLLVMCore/} $OBJ_DIR/libLLVMCore.a $OBJ_DIR/debughack.o"
fi

clang++ -Wl,-allow_sub_type_mismatches -shared $LIBS $OBJ_DIR/wrappers.o $OBJ_DIR/helpers.o -o $SOBJ_DIR/$DLL_NAME -L/usr/local/opt/libffi/lib $LDFLAGS