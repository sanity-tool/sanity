/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package na.okutane.cpp.llvm;

public class SWIGTYPE_p_uint8_t {
  private transient long swigCPtr;

  protected SWIGTYPE_p_uint8_t(long cPtr, @SuppressWarnings("unused") boolean futureUse) {
    swigCPtr = cPtr;
  }

  protected SWIGTYPE_p_uint8_t() {
    swigCPtr = 0;
  }

  protected static long getCPtr(SWIGTYPE_p_uint8_t obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  public boolean equals(Object obj) {
    boolean equal = false;
    if (obj instanceof SWIGTYPE_p_uint8_t)
      equal = (((SWIGTYPE_p_uint8_t)obj).swigCPtr == this.swigCPtr);
    return equal;
  }

  public int hashCode() {
     return Long.hashCode(swigCPtr);
  }
}

