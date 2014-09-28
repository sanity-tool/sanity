%module bitreader

%{
#include <string>
#include "llvm/ADT/Hashing.h"
#include "llvm/ADT/StringRef.h"
#include "llvm/Support/SourceMgr.h"
#include "llvm/DebugInfo.h"
#include "llvm/IR/Type.h"
#include "llvm/IR/Value.h"
#include "llvm/IR/Constant.h"
#include "llvm/IR/Constants.h"
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

%define LISTHELPER(type, itemtype, listname, altname)
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

%define DOWNCAST(fromType, toType)
%{
toType * to ## toType(fromType *o) {
    return (toType*)o;
}
%}

llvm::toType * to ## toType(llvm::fromType *);

%enddef

#define END_WITH_NULL
#define DEFINE_SIMPLE_CONVERSION_FUNCTIONS(A, B)
#define DEFINE_ISA_CONVERSION_FUNCTIONS(A, B)
#define DEFINE_TRANSPARENT_OPERAND_ACCESSORS(A, B)
#define LLVM_DELETED_FUNCTION
#define LLVM_READONLY

%ignore SourceMgr;
%ignore llvm::GlobalValue::use_empty_except_constants;
%ignore llvm::ConstantDataSequential;

%include "std_string.i"

%include "llvm/ADT/StringRef.h"
%include "llvm/ADT/APInt.h"
%include "llvm/ADT/ilist.h"
%include "llvm/IR/LLVMContext.h"
//%include "llvm/DebugInfo.h"
%include "llvm/IR/Type.h"
%include "llvm/IR/Value.h"
%include "llvm/IR/Constant.h"
%include "llvm/IR/Constants.h"
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

DOWNCAST(Value, MDNode);
DOWNCAST(Value, MDString);
DOWNCAST(Value, ConstantInt);