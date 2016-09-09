#!/bin/sh
# Exit on failure
set -e

case `uname` in
    Darwin)
        LLVM_REMOTE="http://llvm.org/releases/3.8.0/clang+llvm-3.8.0-x86_64-apple-darwin.tar.xz"
        #LLVM_REMOTE="http://llvm.org/releases/3.9.0/clang+llvm-3.9.0-x86_64-apple-darwin.tar.xz"

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

        STDLIBS="/usr/lib/libc.dylib /usr/lib/libc++.dylib /usr/lib/libstdc++.dylib /usr/lib/libtermcap.dylib"

        DLL_NAME=libirreader.jnilib
    ;;
    Linux)
        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

        STDLIBS="/usr/lib/x86_64-linux-gnu/libtermcap.so /usr/lib/x86_64-linux-gnu/libc.so /usr/lib/x86_64-linux-gnu/libc++.so /usr/lib/x86_64-linux-gnu/libstdc++.so.6"

        DLL_NAME=libirreader.so
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

TARGET_DIR=../../target
DOWNLOADS_DIR=$TARGET_DIR/downloads
DEPENDENCIES_DIR=$TARGET_DIR/dependencies

# download llvm we're building against
LLVM_LOCAL="$DOWNLOADS_DIR/llvm.tar.xz"
mkdir -p $DOWNLOADS_DIR
[ -f $LLVM_LOCAL ]  || curl $LLVM_REMOTE -o $LLVM_LOCAL

# extract
mkdir -p $DEPENDENCIES_DIR
tar xf $LLVM_LOCAL -C $DEPENDENCIES_DIR

# find
LLVM_CONFIG=`find $TARGET_DIR -name llvm-config`
CC=`find $TARGET_DIR -name clang`
CXX=`find $TARGET_DIR -name clang++`

# check tools availability
$LLVM_CONFIG --version
$CC --version
$CXX --version
swig -version

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS=`$LLVM_CONFIG --ldflags`
LIBS="`$LLVM_CONFIG --libs` -ltermcap"

echo $LIBS

STD_INCLUDES=

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"
LLVM_LIBS=

DEBUG=-g

echo "swig $LLVM_INCLUDE -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v -debug-tmsearch -debug-tmused bitreader.i"

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

clang -c $CPP_OUT/bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $STD_INCLUDES $DEBUG -fPIC -o $OBJ_DIR/wrappers.o

COMPILE_HELPERS="clang++ -c helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $CPPFLAGS -fPIC -std=c++11 -o $OBJ_DIR/helpers.o"
echo $COMPILE_HELPERS
eval $COMPILE_HELPERS

LINK_CMD="clang++ -shared $STD_LIBS $LIBS $OBJ_DIR/wrappers.o $OBJ_DIR/helpers.o -o $SOBJ_DIR/$DLL_NAME $LDFLAGS"
echo $LINK_CMD
eval $LINK_CMD

#ldd $DLL_NAME
#nm --dynamic --undefined-only $DLL_NAME