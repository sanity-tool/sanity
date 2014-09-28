%module bitreader

%{
#include <string>
#include "llvm/Support/SourceMgr.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"
#include "llvm/IRReader/IRReader.h"
using namespace llvm;

Module *parse(const char *filename) {
    SMDiagnostic err;
    Module *result = getLazyIRFileModule(filename, err, getGlobalContext());
    return result;
}
%}

#define END_WITH_NULL
#define DEFINE_SIMPLE_CONVERSION_FUNCTIONS(A, B)
#define LLVM_DELETED_FUNCTION

%ignore SourceMgr;

%include "llvm/IR/LLVMContext.h"
%include "llvm/IR/Module.h"
%include "llvm/IRReader/IRReader.h"

llvm::Module *parse(const char *filename);