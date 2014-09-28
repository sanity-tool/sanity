#!/bin/sh

OUTDIR="../../src/na/okutane/cpp/llvm"

rm $OUTDIR/*.java

JAVA_INCLUDES="-I/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/include/ -I/Library/Java/JavaVirtualMachines/jdk1.8.0_05.jdk/Contents/Home/include/darwin/"

STD_INCLUDES="-I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk/usr/include/ -I/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX10.9.sdk/usr/include/c++/4.2.1/"

DLL_NAME=libirreader.jnilib

#use clang
LLVM_INCLUDE="-I/Users/jondoe/Downloads/clang+llvm-3.3-x86_64-apple-darwin12/include"
LLVM_LIBS=/Users/jondoe/Downloads/clang+llvm-3.3-x86_64-apple-darwin12/lib/*.a

#use llvm
#LLVM_INCLUDE="-I/Users/jondoe/Downloads/llvm-3.5.0.src/include"
#LLVM_LIBS=/Users/jondoe/Downloads/llvm-3.5.0.src/Release+Asserts/lib/*.a

swig $LLVM_INCLUDE $STD_INCLUDES -c++ -java -outdir $OUTDIR -package na.okutane.cpp.llvm bitreader.i
g++ -c bitreader_wrap.cxx -D__STDC_CONSTANT_MACROS -D__STDC_LIMIT_MACROS $JAVA_INCLUDES $LLVM_INCLUDE $STD_INCLUDES
g++ -shared bitreader_wrap.o $LLVM_LIBS /usr/lib/libc.dylib /usr/lib/libc++.dylib /usr/lib/libstdc++.dylib /usr/lib/libtermcap.dylib -o $DLL_NAME

rm *.o
rm *.cxx
