/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package na.okutane.cpp.llvm;

public class SWIGTYPE_p_p_LLVMOpaqueValue {
  private long swigCPtr;

  protected SWIGTYPE_p_p_LLVMOpaqueValue(long cPtr, boolean futureUse) {
    swigCPtr = cPtr;
  }

  protected SWIGTYPE_p_p_LLVMOpaqueValue() {
    swigCPtr = 0;
  }

  protected static long getCPtr(SWIGTYPE_p_p_LLVMOpaqueValue obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof SWIGTYPE_p_p_LLVMOpaqueValue)
      equal = (((SWIGTYPE_p_p_LLVMOpaqueValue)obj).swigCPtr == this.swigCPtr);
    return equal;
  }
  
  public int hashCode() {
     return Long.hashCode(swigCPtr);
  }
}

