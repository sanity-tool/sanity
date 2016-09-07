#include "llvm-c/Core.h"
#include "llvm/ADT/APFloat.h"
#include "llvm/Bitcode/ReaderWriter.h"
#include "llvm/IR/Attributes.h"
//#include "llvm/IR/CallSite.h"
#include "llvm/IR/Constants.h"
#include "llvm/IR/DerivedTypes.h"
//#include "llvm/IR/DiagnosticInfo.h"
//#include "llvm/IR/DiagnosticPrinter.h"
#include "llvm/IR/GlobalAlias.h"
#include "llvm/IR/GlobalVariable.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/InlineAsm.h"
#include "llvm/IR/IntrinsicInst.h"
#include "llvm/IR/LLVMContext.h"
#include "llvm/IR/Module.h"
#include "llvm/PassManager.h"
#include "llvm/Support/Debug.h"
#include "llvm/Support/ErrorHandling.h"
#include "llvm/Support/FileSystem.h"
#include "llvm/Support/ManagedStatic.h"
#include "llvm/Support/MemoryBuffer.h"
#include "llvm/Support/Threading.h"
#include "llvm/Support/raw_ostream.h"

#include <iostream>
#include <stdio.h>

using namespace llvm;

extern "C" {

const char *GetDataArrayString(LLVMValueRef Val) {
    Value *V = unwrap(Val);

    if (ConstantDataSequential *CDS = dyn_cast<ConstantDataSequential>(V)) {
        return CDS->getAsString().data();
    }
    return 0;
}

LLVMRealPredicate GetFCmpPredicate(LLVMValueRef Inst) {
  if (FCmpInst *I = dyn_cast<FCmpInst>(unwrap(Inst)))
    return (LLVMRealPredicate)I->getPredicate();
  if (ConstantExpr *CE = dyn_cast<ConstantExpr>(unwrap(Inst)))
    if (CE->getOpcode() == Instruction::FCmp)
      return (LLVMRealPredicate)CE->getPredicate();
  return (LLVMRealPredicate)0;
}

#define checkSemantics(kind) if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::kind) \
    fprintf(stderr, "semantics not supported: %s\n", #kind);

double GetConstantFPDoubleValue(LLVMValueRef ConstantVal) {
  const APFloat &apf = unwrap<ConstantFP>(ConstantVal)->getValueAPF();
  /*const llvm::fltSemantics *semantics = &apf.getSemantics();
  if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::IEEEdouble) {
    return apf.convertToDouble();
  }
  if (semantics == (const llvm::fltSemantics*)&llvm::APFloat::IEEEsingle) {
    return apf.convertToFloat();
  }

  checkSemantics(IEEEhalf);
  checkSemantics(IEEEquad);
  checkSemantics(PPCDoubleDouble);
  checkSemantics(x87DoubleExtended);*/

  return apf.bitcastToAPInt().bitsToDouble();
}

}