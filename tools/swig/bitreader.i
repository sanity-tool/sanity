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
#include "llvm/IR/Instructions.h"
#include "llvm/IR/BasicBlock.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Module.h"
#include "llvm/IRReader/IRReader.h"

#include "llvm/Support/raw_ostream.h"

using namespace llvm;

Module *parse(const char *filename) {
    SMDiagnostic err;
    Module *result = getLazyIRFileModule(filename, err, getGlobalContext());
    return result;
}

const char *getName(GlobalValue *gv) {
    return gv->getName().begin();
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

using namespace llvm;

%extend llvm::Value {
    std::string toString() {
        std::string result;
        raw_string_ostream out(result);
        $self->print(out);
        return out.str();
    }
};

class StoreInst : public Instruction {
  void *operator new(size_t, unsigned) LLVM_DELETED_FUNCTION;
  void AssertOK();
protected:
  virtual StoreInst *clone_impl() const;
public:
  // allocate space for exactly two operands
  void *operator new(size_t s) {
    return User::operator new(s, 2);
  }
  StoreInst(Value *Val, Value *Ptr, Instruction *InsertBefore);
  StoreInst(Value *Val, Value *Ptr, BasicBlock *InsertAtEnd);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile = false,
            Instruction *InsertBefore = 0);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile, BasicBlock *InsertAtEnd);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile,
            unsigned Align, Instruction *InsertBefore = 0);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile,
            unsigned Align, BasicBlock *InsertAtEnd);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile,
            unsigned Align, AtomicOrdering Order,
            SynchronizationScope SynchScope = CrossThread,
            Instruction *InsertBefore = 0);
  StoreInst(Value *Val, Value *Ptr, bool isVolatile,
            unsigned Align, AtomicOrdering Order,
            SynchronizationScope SynchScope,
            BasicBlock *InsertAtEnd);


  /// isVolatile - Return true if this is a store to a volatile memory
  /// location.
  ///
  bool isVolatile() const { return getSubclassDataFromInstruction() & 1; }

  /// setVolatile - Specify whether this is a volatile store or not.
  ///
  void setVolatile(bool V) {
    setInstructionSubclassData((getSubclassDataFromInstruction() & ~1) |
                               (V ? 1 : 0));
  }

  /// Transparently provide more efficient getOperand methods.
  DECLARE_TRANSPARENT_OPERAND_ACCESSORS(Value);

  /// getAlignment - Return the alignment of the access that is being performed
  ///
  unsigned getAlignment() const {
    return (1 << ((getSubclassDataFromInstruction() >> 1) & 31)) >> 1;
  }

  void setAlignment(unsigned Align);

  /// Returns the ordering effect of this store.
  AtomicOrdering getOrdering() const {
    return AtomicOrdering((getSubclassDataFromInstruction() >> 7) & 7);
  }

  /// Set the ordering constraint on this store.  May not be Acquire or
  /// AcquireRelease.
  void setOrdering(AtomicOrdering Ordering) {
    setInstructionSubclassData((getSubclassDataFromInstruction() & ~(7 << 7)) |
                               (Ordering << 7));
  }

  SynchronizationScope getSynchScope() const {
    return SynchronizationScope((getSubclassDataFromInstruction() >> 6) & 1);
  }

  /// Specify whether this store instruction is ordered with respect to all
  /// concurrently executing threads, or only with respect to signal handlers
  /// executing in the same thread.
  void setSynchScope(SynchronizationScope xthread) {
    setInstructionSubclassData((getSubclassDataFromInstruction() & ~(1 << 6)) |
                               (xthread << 6));
  }

  bool isAtomic() const { return getOrdering() != NotAtomic; }
  void setAtomic(AtomicOrdering Ordering,
                 SynchronizationScope SynchScope = CrossThread) {
    setOrdering(Ordering);
    setSynchScope(SynchScope);
  }

  bool isSimple() const { return !isAtomic() && !isVolatile(); }
  bool isUnordered() const {
    return getOrdering() <= Unordered && !isVolatile();
  }

  Value *getValueOperand() { return getOperand(0); }
  const Value *getValueOperand() const { return getOperand(0); }

  Value *getPointerOperand() { return getOperand(1); }
  const Value *getPointerOperand() const { return getOperand(1); }
  static unsigned getPointerOperandIndex() { return 1U; }

  /// \brief Returns the address space of the pointer operand.
  unsigned getPointerAddressSpace() const {
    return getPointerOperand()->getType()->getPointerAddressSpace();
  }

  // Methods for support type inquiry through isa, cast, and dyn_cast:
  static inline bool classof(const Instruction *I) {
    return I->getOpcode() == Instruction::Store;
  }
  static inline bool classof(const Value *V) {
    return isa<Instruction>(V) && classof(cast<Instruction>(V));
  }
private:
  // Shadow Instruction::setInstructionSubclassData with a private forwarding
  // method so that subclasses cannot accidentally use it.
  void setInstructionSubclassData(unsigned short D) {
    Instruction::setInstructionSubclassData(D);
  }
};

llvm::Module *parse(const char *filename);
const char *getName(llvm::GlobalValue *gv);

LISTHELPER(llvm::Module, llvm::Function, getFunctionList, getModuleFunctions);
LISTHELPER(llvm::BasicBlock, llvm::Instruction, getInstList, getBasicBlockInstructions);

DOWNCAST(Value, MDNode);
DOWNCAST(Value, MDString);
DOWNCAST(Value, ConstantInt);
DOWNCAST(Instruction, StoreInst);