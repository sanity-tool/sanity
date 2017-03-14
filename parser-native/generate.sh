#!/bin/bash

# Exit on failure
set -e

case `uname` in
    Linux)
        if [[ ! -f "cmake-3.4.3-Linux-x86_64/bin/cmake" ]]; then wget --no-check-certificate http://cmake.org/files/v3.4/cmake-3.4.3-Linux-x86_64.tar.gz && tar -xvf cmake-3.4.3-Linux-x86_64.tar.gz; fi
        CMAKE=`pwd`/cmake-3.4.3-Linux-x86_64/bin/cmake
        CC=gcc-4.9
        export CXX=g++-4.9
        LD=g++-4.9

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

        DLL_NAME=libirreader.so

        LDFLAGS="-lpthread -ltermcap"

        # todo nice to have
        #LDFLAGS="$LDFLAGS -Wl,-z,defs"
    ;;
    Darwin)
        CMAKE=cmake
        CC=clang
        CXX=clang++
        LD=clang++

        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

        DLL_NAME=libirreader.jnilib

        LDFLAGS="-ltermcap -L/usr/local/opt/libffi/lib"
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

LLVM_HOME="target/llvm"
LLVM_CONFIG=$LLVM_HOME/build/bin/llvm-config

if [ ! -d "$LLVM_HOME" ] ; then
    OLD_DIR=`pwd`

    git clone -b saving-debug https://github.com/okutane/llvm.git $LLVM_HOME
    cd $LLVM_HOME

    mkdir build && cd build
    $CMAKE -G "Unix Makefiles" ..
    # subdependencies for my library
    make LLVMCore LLVMAsmParser LLVMBitReader LLVMProfileData LLVMMC LLVMMCParser LLVMObject LLVMAnalysis
    # dependencies for my library
    make LLVMIRReader LLVMTransformUtils
    # to build my library
    make llvm-config
    # to check strip-debug-info
    make llvm-dis

    LLVM_CONFIG=$LLVM_HOME/build/bin/llvm-config

    cd $OLD_DIR
fi

echo `$LLVM_CONFIG --version`

LLVM_LIBS="irreader transformutils"
LIBS=`$LLVM_CONFIG --libs $LLVM_LIBS`

CFLAGS=`$LLVM_CONFIG --cflags`
CXXFLAGS=`$LLVM_CONFIG --cxxflags`

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS="`$LLVM_CONFIG --ldflags` -v $LDFLAGS"

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"

DEBUG="-g -coverage"
COMMONFLAGS="$DEBUG"

SRC_DIR="src/main/cpp"

JAVA_OUT="target/generated-sources/java/ru/urururu/sanity/cpp/llvm"
mkdir -p $JAVA_OUT

CPP_OUT="target/generated-sources/jni"
mkdir -p $CPP_OUT

OBJ_DIR="target/native/static"
mkdir -p $OBJ_DIR

SOBJ_DIR="target/native/shared"
mkdir -p $SOBJ_DIR

swig $CPPFLAGS -java -outdir $JAVA_OUT -package ru.urururu.sanity.cpp.llvm -o $CPP_OUT/bitreader_wrap.c -v $SRC_DIR/bitreader.i

$CC -c $CPP_OUT/bitreader_wrap.c $JAVA_INCLUDES $CFLAGS $COMMONFLAGS -o $OBJ_DIR/wrappers.o

$CXX -c $SRC_DIR/helpers.cpp $CXXFLAGS $COMMONFLAGS -o $OBJ_DIR/helpers.o

$CXX -shared -o $SOBJ_DIR/$DLL_NAME $OBJ_DIR/wrappers.o $OBJ_DIR/helpers.o $LIBS $LDFLAGS $DEBUG