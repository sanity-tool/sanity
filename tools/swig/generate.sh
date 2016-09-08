#!/bin/sh
# Exit on failure
#set -e

LLVM_CONFIG=llvm-config
$LLVM_CONFIG --version >/dev/null 2>&1 || LLVM_CONFIG=/usr/local/opt/llvm/bin/llvm-config

$LLVM_CONFIG --version

$LLVM_CONFIG --includedir

$LLVM_CONFIG --components
$LLVM_CONFIG --libdir
$LLVM_CONFIG --cxxflags
$LLVM_CONFIG --ldflags

#sudo apt-get -qq update
swig -version >/dev/null 2>&1 || sudo apt-get install -y swig

swig -version

OUTDIR="../../src/main/java/na/okutane/cpp/llvm"

rm -rf $OUTDIR || echo already removed


case `uname` in
    Darwin)
        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

        STDLIBS="/usr/lib/libc.dylib /usr/lib/libc++.dylib /usr/lib/libstdc++.dylib /usr/lib/libtermcap.dylib"

        DLL_NAME=libirreader.jnilib
    ;;
    Linux)
        JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"

        STDLIBS="/usr/lib/x86_64-linux-gnu/libtermcap.so /usr/lib/x86_64-linux-gnu/libstdc++.so.6"

        DLL_NAME=libirreader.so
    ;;
    *)
        echo Unknown environment: `uname`
        exit 1
    ;;
esac

find / 2>/dev/null|grep libc\\.

find / 2>/dev/null|grep libstdc

LLVM_MODULES="core native"

CPPFLAGS=`$LLVM_CONFIG --cppflags`
LDFLAGS=`$LLVM_CONFIG --ldflags`
LIBS=`$LLVM_CONFIG --libs $LLVM_MODULES`

echo $LIBS

STD_INCLUDES=

LLVM_INCLUDE="-I`$LLVM_CONFIG --includedir`"
LLVM_LIBS=

DEBUG=-g

echo "swig $LLVM_INCLUDE -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v -debug-tmsearch -debug-tmused bitreader.i"

mkdir $OUTDIR
swig $LLVM_INCLUDE -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v bitreader.i
swig -E $LLVM_INCLUDE $STD_INCLUDES -java bitreader.i > swigprep.txt

clang -c bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $STD_INCLUDES $DEBUG -fPIC

clang++ -c helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $CPPFLAGS -fPIC

clang++ -shared bitreader_wrap.o helpers.o -o $DLL_NAME $LDFLAGS $LIBS $STD_LIBS

ldd $DLL_NAME
nm --dynamic --undefined-only $DLL_NAME