%module bitreader

%{
#include "llvm-c/IRReader.h"

LLVMModuleRef parse(const char *path) {
    LLVMModuleRef m;
    LLVMMemoryBufferRef membuf;
    char *errmsg;
    LLVMContextRef ctx = LLVMGetGlobalContext();

    if (LLVMCreateMemoryBufferWithContentsOfFile(path, &membuf, &errmsg)) {
        return 0;
    }
    if (LLVMParseIRInContext(ctx, membuf, &m, &errmsg)) {
        return 0;
    }
    return m;
}

const char *getMDString(LLVMValueRef valueRef) {
    unsigned int len;
    return LLVMGetMDString(valueRef, &len);
}

%}

#define __STDC_LIMIT_MACROS
#define __STDC_CONSTANT_MACROS

%include "llvm/Support/DataTypes.h"
%include "llvm-c/Support.h"
%include "llvm-c/Core.h"
%include "llvm-c/IRReader.h"

LLVMModuleRef parse(const char *path);
const char *getMDString(LLVMValueRef valueRef);
