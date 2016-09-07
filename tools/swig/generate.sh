#!/bin/sh
# Exit on failure
set -e

OUTDIR="../../src/main/java/na/okutane/cpp/llvm"

rm $OUTDIR/*.java

JAVA_INCLUDES="-I/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/include/ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/include/darwin/"

STD_INCLUDES="-I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk/usr/include/ -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk/usr/include/c++/4.2.1/"

DLL_NAME=libirreader.jnilib

#use release build of llvm
LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src_hacked/include"
LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src_hacked/Release+Asserts/lib/*.a

#use clang
#LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src/include"
#LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src/Release+Asserts/lib/*.a

#use llvm
#LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src/include"
#LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src/Release+Asserts/lib/*.a

DEBUG=-g

swig $LLVM_INCLUDE $STD_INCLUDES -java -outdir $OUTDIR -package na.okutane.cpp.llvm -v -debug-tmsearch -debug-tmused bitreader.i > swigout.txt
swig -E $LLVM_INCLUDE $STD_INCLUDES -java bitreader.i > swigprep.txt
gcc -c bitreader_wrap.c -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $STD_INCLUDES -ferror-limit=1 $DEBUG
g++ -c helpers.cpp -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE -ferror-limit=1 -std=gnu++11 $DEBUG
gcc -shared bitreader_wrap.o helpers.o $LLVM_LIBS /usr/lib/libc.dylib /usr/lib/libc++.dylib /usr/lib/libstdc++.dylib /usr/lib/libtermcap.dylib -o $DLL_NAME

rm *.o
#rm *.cxx
