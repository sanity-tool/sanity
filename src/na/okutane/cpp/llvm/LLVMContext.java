/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package na.okutane.cpp.llvm;

public class LLVMContext {
  private long swigCPtr;
  protected boolean swigCMemOwn;

  protected LLVMContext(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(LLVMContext obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        bitreaderJNI.delete_LLVMContext(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public SWIGTYPE_p_llvm__LLVMContextImpl getPImpl() {
    long cPtr = bitreaderJNI.LLVMContext_pImpl_get(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_llvm__LLVMContextImpl(cPtr, false);
  }

  public LLVMContext() {
    this(bitreaderJNI.new_LLVMContext(), true);
  }

  public long getMDKindID(SWIGTYPE_p_llvm__StringRef Name) {
    return bitreaderJNI.LLVMContext_getMDKindID(swigCPtr, this, SWIGTYPE_p_llvm__StringRef.getCPtr(Name));
  }

  public void getMDKindNames(SWIGTYPE_p_llvm__SmallVectorImplT_llvm__StringRef_t Result) {
    bitreaderJNI.LLVMContext_getMDKindNames(swigCPtr, this, SWIGTYPE_p_llvm__SmallVectorImplT_llvm__StringRef_t.getCPtr(Result));
  }

  public void setInlineAsmDiagnosticHandler(SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void DiagHandler, SWIGTYPE_p_void DiagContext) {
    bitreaderJNI.LLVMContext_setInlineAsmDiagnosticHandler__SWIG_0(swigCPtr, this, SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void.getCPtr(DiagHandler), SWIGTYPE_p_void.getCPtr(DiagContext));
  }

  public void setInlineAsmDiagnosticHandler(SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void DiagHandler) {
    bitreaderJNI.LLVMContext_setInlineAsmDiagnosticHandler__SWIG_1(swigCPtr, this, SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void.getCPtr(DiagHandler));
  }

  public SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void getInlineAsmDiagnosticHandler() {
    long cPtr = bitreaderJNI.LLVMContext_getInlineAsmDiagnosticHandler(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_f_r_q_const__llvm__SMDiagnostic_p_void_unsigned_int__void(cPtr, false);
  }

  public SWIGTYPE_p_void getInlineAsmDiagnosticContext() {
    long cPtr = bitreaderJNI.LLVMContext_getInlineAsmDiagnosticContext(swigCPtr, this);
    return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
  }

  public void emitError(long LocCookie, SWIGTYPE_p_llvm__Twine ErrorStr) {
    bitreaderJNI.LLVMContext_emitError__SWIG_0(swigCPtr, this, LocCookie, SWIGTYPE_p_llvm__Twine.getCPtr(ErrorStr));
  }

  public void emitError(SWIGTYPE_p_llvm__Instruction I, SWIGTYPE_p_llvm__Twine ErrorStr) {
    bitreaderJNI.LLVMContext_emitError__SWIG_1(swigCPtr, this, SWIGTYPE_p_llvm__Instruction.getCPtr(I), SWIGTYPE_p_llvm__Twine.getCPtr(ErrorStr));
  }

  public void emitError(SWIGTYPE_p_llvm__Twine ErrorStr) {
    bitreaderJNI.LLVMContext_emitError__SWIG_2(swigCPtr, this, SWIGTYPE_p_llvm__Twine.getCPtr(ErrorStr));
  }

  public final static int MD_dbg = bitreaderJNI.LLVMContext_MD_dbg_get();
  public final static int MD_tbaa = bitreaderJNI.LLVMContext_MD_tbaa_get();
  public final static int MD_prof = bitreaderJNI.LLVMContext_MD_prof_get();
  public final static int MD_fpmath = bitreaderJNI.LLVMContext_MD_fpmath_get();
  public final static int MD_range = bitreaderJNI.LLVMContext_MD_range_get();
  public final static int MD_tbaa_struct = bitreaderJNI.LLVMContext_MD_tbaa_struct_get();
  public final static int MD_invariant_load = bitreaderJNI.LLVMContext_MD_invariant_load_get();

}
