#!/bin/sh
# Exit on failure
#set -e

llvm-config --includedir

llvm-config --components
llvm-config --libdir
llvm-config --cxxflags
llvm-config --ldflags

sudo apt-get -qq update
sudo apt-get install -y swig

OUTDIR="../../src/main/java/na/okutane/cpp/llvm"

rm $OUTDIR/*.java || echo already removed

JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/linux/"
#JAVA_INCLUDES="-I$JAVA_HOME/include/ -I$JAVA_HOME/include/darwin/"

STD_INCLUDES=

DLL_NAME=irreader.so

#sudo apt-get install gcc-4.7
#wget -nc http://llvm.org/releases/3.5.0/llvm-3.5.0.src.tar.xz
#tar xf llvm-3.5.0.src.tar.xz
#cd llvm-3.5.0.src
#./configure
#make install

#apt-get install llvm

#use release build of llvm
LLVM_INCLUDE="-I`llvm-config --includedir`"
LLVM_LIBS=

#use clang
#LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src/include"
#LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src/Release+Asserts/lib/*.a

#use llvm
#LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src/include"
#LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src/Release+Asserts/lib/*.a

DEBUG=-g

echo "swig $LLVM_INCLUDE -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v -debug-tmsearch -debug-tmused bitreader.i"

swig $LLVM_INCLUDE -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v -debug-tmsearch -debug-tmused bitreader.i
swig -E $LLVM_INCLUDE $STD_INCLUDES -java bitreader.i > swigprep.txt

gcc -c bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $STD_INCLUDES $DEBUG
g++ -c helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $DEBUG -fPIC
gcc -shared bitreader_wrap.o helpers.o $LLVM_LIBS -o $DLL_NAME

#rm *.o
#rm *.cxx
