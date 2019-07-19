set LLVM_HOME="C:\Progra~2\LLVM"

set SRC_DIR="src/main/cpp"

set JAVA_OUT="target/generated-sources/java/ru/urururu/sanity/cpp/libclang"
mkdir %JAVA_OUT%

set CPP_OUT="target/generated-sources/jni"
mkdir %CPP_OUT%

set OBJ_DIR="target/native/static"
mkdir %OBJ_DIR%

set SOBJ_DIR="target/native/shared"
mkdir %SOBJ_DIR%

set CFLAGS=-I%LLVM_HOME%\include

clang -E %CFLAGS% %SRC_DIR%/fake.c -o %CPP_OUT%/fake.h

swig %CFLAGS% -java -outdir %JAVA_OUT% -package ru.urururu.sanity.cpp.libclang -o %CPP_OUT%/libclang_wrap.c -v %SRC_DIR%/libclang.i

clang -c %CPP_OUT%/libclang_wrap.c %JAVA_INCLUDES% %CFLAGS% %COMMONFLAGS% -o %OBJ_DIR%/wrappers.o

clang++ -shared -o %SOBJ_DIR%/%DLL_NAME% %OBJ_DIR%/wrappers.o %OBJ_DIR%/helpers.o %LIBS% %LDFLAGS% %DEBUG%
