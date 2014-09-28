; ModuleID = 'res/rules/NP/basic.c'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.9.0"

define void @test() nounwind ssp uwtable {
  %p = alloca i32*, align 8
  store i32* null, i32** %p, align 8, !dbg !9
  %1 = load i32** %p, align 8, !dbg !10
  store i32 1, i32* %1, align 4, !dbg !10
  ret void, !dbg !11
}

define void @testConditional(i32* %p) nounwind ssp uwtable {
  %1 = alloca i32*, align 8
  store i32* %p, i32** %1, align 8
  %2 = load i32** %1, align 8, !dbg !12
  %3 = icmp ne i32* %2, null, !dbg !12
  br i1 %3, label %4, label %5, !dbg !12

; <label>:4                                       ; preds = %0
  br label %5, !dbg !12

; <label>:5                                       ; preds = %4, %0
  %6 = load i32** %1, align 8, !dbg !13
  store i32 1, i32* %6, align 4, !dbg !13
  ret void, !dbg !14
}

!llvm.dbg.cu = !{!0}

!0 = metadata !{i32 786449, i32 0, i32 12, metadata !"res/rules/NP/basic.c", metadata !"/Users/jondoe/IdeaProjects/SA/sanity/tests", metadata !"Apple LLVM version 5.0 (clang-500.2.79) (based on LLVM 3.3svn)", i1 true, i1 false, metadata !"", i32 0, metadata !1, metadata !1, metadata !3, metadata !1} ; [ DW_TAG_compile_unit ] [/Users/jondoe/IdeaProjects/SA/sanity/tests/res/rules/NP/basic.c] [DW_LANG_C99]
!1 = metadata !{metadata !2}
!2 = metadata !{i32 0}
!3 = metadata !{metadata !4}
!4 = metadata !{metadata !5, metadata !8}
!5 = metadata !{i32 786478, i32 0, metadata !6, metadata !"test", metadata !"test", metadata !"", metadata !6, i32 1, metadata !7, i1 false, i1 true, i32 0, i32 0, null, i32 0, i1 false, void ()* @test, null, null, metadata !1, i32 1} ; [ DW_TAG_subprogram ] [line 1] [def] [test]
!6 = metadata !{i32 786473, metadata !"res/rules/NP/basic.c", metadata !"/Users/jondoe/IdeaProjects/SA/sanity/tests", null} ; [ DW_TAG_file_type ]
!7 = metadata !{i32 786453, i32 0, metadata !"", i32 0, i32 0, i64 0, i64 0, i64 0, i32 0, null, metadata !2, i32 0, i32 0} ; [ DW_TAG_subroutine_type ] [line 0, size 0, align 0, offset 0] [from ]
!8 = metadata !{i32 786478, i32 0, metadata !6, metadata !"testConditional", metadata !"testConditional", metadata !"", metadata !6, i32 6, metadata !7, i1 false, i1 true, i32 0, i32 0, null, i32 256, i1 false, void (i32*)* @testConditional, null, null, metadata !1, i32 6} ; [ DW_TAG_subprogram ] [line 6] [def] [testConditional]
!9 = metadata !{i32 2, i32 0, metadata !5, null}
!10 = metadata !{i32 3, i32 0, metadata !5, null}
!11 = metadata !{i32 4, i32 0, metadata !5, null}
!12 = metadata !{i32 7, i32 0, metadata !8, null}
!13 = metadata !{i32 8, i32 0, metadata !8, null}
!14 = metadata !{i32 9, i32 0, metadata !8, null}
