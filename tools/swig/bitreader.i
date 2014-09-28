%module bitreader

%{
#include <string>
#include "llvm/ADT/Hashing.h"
#include "llvm/ADT/StringRef.h"
#include "llvm/Support/SourceMgr.h"
#include "llvm/DebugInfo.h"
#include "llvm/IR/Metadata.h"
#include "llvm/Support/DebugLoc.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Operator.h"
#include "llvm/IR/Instruction.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Module.h"
#include "llvm/IRReader/IRReader.h"

using namespace llvm;

Module *parse(const char *filename) {
    SMDiagnostic err;
    Module *result = getLazyIRFileModule(filename, err, getGlobalContext());
    return result;
}
%}

%define LISTHELPER(type,itemtype,listname,altname)
%{

int altname ## Size(type *o) {
    return o->listname().size();
}

itemtype *altname ## Item(type *o, int index) {
    ilist_iterator<itemtype> it = o->listname().begin();
    while(index) {
        it++;
        index--;
    }
    return it;
}
%}

int altname ## Size(type *o);
itemtype *altname ## Item(type *o, int index);

%enddef

%include "std_string.i"
%include "std_map.i"

#define END_WITH_NULL
#define DEFINE_SIMPLE_CONVERSION_FUNCTIONS(A, B)
#define LLVM_DELETED_FUNCTION
#define LLVM_READONLY

%ignore SourceMgr;
%ignore llvm::GlobalValue::use_empty_except_constants;

%include "llvm/ADT/StringRef.h"
%include "llvm/ADT/ilist.h"
%include "llvm/IR/LLVMContext.h"
%include "llvm/DebugInfo.h"
%include "llvm/IR/Metadata.h"
%include "llvm/Support/DebugLoc.h"
%include "llvm/IR/Instruction.h"
%include "llvm/IR/BasicBlock.h"
%include "llvm/IR/GlobalValue.h"
%include "llvm/IR/Function.h"
%include "llvm/IR/Module.h"
%include "llvm/IRReader/IRReader.h"

llvm::Module *parse(const char *filename);

LISTHELPER(llvm::Module, llvm::Function, getFunctionList, getModuleFunctions);
LISTHELPER(llvm::BasicBlock, llvm::Instruction, getInstList, getBasicBlockInstructions);