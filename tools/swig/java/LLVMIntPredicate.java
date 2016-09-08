/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.10
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package na.okutane.cpp.llvm;

public final class LLVMIntPredicate {
  public final static LLVMIntPredicate LLVMIntEQ = new LLVMIntPredicate("LLVMIntEQ", bitreaderJNI.LLVMIntEQ_get());
  public final static LLVMIntPredicate LLVMIntNE = new LLVMIntPredicate("LLVMIntNE");
  public final static LLVMIntPredicate LLVMIntUGT = new LLVMIntPredicate("LLVMIntUGT");
  public final static LLVMIntPredicate LLVMIntUGE = new LLVMIntPredicate("LLVMIntUGE");
  public final static LLVMIntPredicate LLVMIntULT = new LLVMIntPredicate("LLVMIntULT");
  public final static LLVMIntPredicate LLVMIntULE = new LLVMIntPredicate("LLVMIntULE");
  public final static LLVMIntPredicate LLVMIntSGT = new LLVMIntPredicate("LLVMIntSGT");
  public final static LLVMIntPredicate LLVMIntSGE = new LLVMIntPredicate("LLVMIntSGE");
  public final static LLVMIntPredicate LLVMIntSLT = new LLVMIntPredicate("LLVMIntSLT");
  public final static LLVMIntPredicate LLVMIntSLE = new LLVMIntPredicate("LLVMIntSLE");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static LLVMIntPredicate swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + LLVMIntPredicate.class + " with value " + swigValue);
  }

  private LLVMIntPredicate(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private LLVMIntPredicate(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private LLVMIntPredicate(String swigName, LLVMIntPredicate swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static LLVMIntPredicate[] swigValues = { LLVMIntEQ, LLVMIntNE, LLVMIntUGT, LLVMIntUGE, LLVMIntULT, LLVMIntULE, LLVMIntSGT, LLVMIntSGE, LLVMIntSLT, LLVMIntSLE };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
