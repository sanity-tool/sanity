%module libclang

%{
#include <clang-c/Index.h>
%}

%include "../../../target/generated-sources/jni/fake.h"

%pragma(java) jniclasscode=%{
  static {
     System.loadLibrary("libclang");
  }
%}