@F0 = internal global float 0.000000e+00, align 4
@F1 = internal global float 0.000000e+00, align 4
@FR = internal global float 0.000000e+00, align 4
@D0 = internal global double 0.000000e+00, align 8
@D1 = internal global double 0.000000e+00, align 8
@DR = internal global double 0.000000e+00, align 8
@LD0 = internal global x86_fp80 0xK00000000000000000000, align 16
@LD1 = internal global x86_fp80 0xK00000000000000000000, align 16
@LDR = internal global x86_fp80 0xK00000000000000000000, align 16

define internal void @testFloatFRem() #0 {
  %1 = load float* @F0, align 4
  %2 = load float* @F1, align 4
  %3 = frem float %1, %2
  store float %3, float* @FR, align 4
  ret void
}

define internal void @testDoubleFRem() #0 {
  %1 = load double* @D0, align 8
  %2 = load double* @D1, align 8
  %3 = frem double %1, %2
  store double %3, double* @DR, align 8
  ret void
}

define internal void @testFP80FRem() #0 {
  %1 = load x86_fp80* @LD0, align 16
  %2 = load x86_fp80* @LD1, align 16
  %3 = frem x86_fp80 %1, %2
  store x86_fp80 %3, x86_fp80* @LDR, align 16
  ret void
}
