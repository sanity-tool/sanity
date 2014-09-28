; ModuleID = 'globals.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.9.0"

@GLOBAL = common global i32* null, align 8

define void @testCondition() nounwind ssp uwtable {
  store i32* null, i32** @GLOBAL, align 8
  %1 = load i32** @GLOBAL, align 8
  store i32 1, i32* %1, align 4
  ret void
}
