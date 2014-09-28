; ModuleID = 'methodCalls.cpp'
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"
target triple = "x86_64-apple-macosx10.9.0"

%struct.Foo = type { i32 (...)** }

define void @_Z18instanceMethodCallP3Foo(%struct.Foo* %p) ssp uwtable {
  %1 = alloca %struct.Foo*, align 8
  store %struct.Foo* %p, %struct.Foo** %1, align 8
  %2 = load %struct.Foo** %1, align 8, !dbg !10
  call void @_ZN3Foo14instanceMethodEv(%struct.Foo* %2), !dbg !10
  ret void, !dbg !11
}

declare void @_ZN3Foo14instanceMethodEv(%struct.Foo*)

define void @_Z16staticMethodCallv() ssp uwtable {
  call void @_ZN3Foo12staticMethodEv(), !dbg !12
  ret void, !dbg !13
}

declare void @_ZN3Foo12staticMethodEv()

define void @_Z18abstractMethodCallP3Foo(%struct.Foo* %p) ssp uwtable {
  %1 = alloca %struct.Foo*, align 8
  store %struct.Foo* %p, %struct.Foo** %1, align 8
  %2 = load %struct.Foo** %1, align 8, !dbg !14
  %3 = bitcast %struct.Foo* %2 to void (%struct.Foo*)***, !dbg !14
  %4 = load void (%struct.Foo*)*** %3, !dbg !14
  %5 = getelementptr inbounds void (%struct.Foo*)** %4, i64 0, !dbg !14
  %6 = load void (%struct.Foo*)** %5, !dbg !14
  call void %6(%struct.Foo* %2), !dbg !14
  ret void, !dbg !15
}

!llvm.dbg.cu = !{!0}

!0 = metadata !{i32 786449, i32 0, i32 4, metadata !"methodCalls.cpp", metadata !"/Users/jondoe/IdeaProjects/SA/sanity/tests/res/cfg", metadata !"Apple LLVM version 5.0 (clang-500.2.79) (based on LLVM 3.3svn)", i1 true, i1 false, metadata !"", i32 0, metadata !1, metadata !1, metadata !3, metadata !1} ; [ DW_TAG_compile_unit ] [/Users/jondoe/IdeaProjects/SA/sanity/tests/res/cfg/methodCalls.cpp] [DW_LANG_C_plus_plus]
!1 = metadata !{metadata !2}
!2 = metadata !{i32 0}
!3 = metadata !{metadata !4}
!4 = metadata !{metadata !5, metadata !8, metadata !9}
!5 = metadata !{i32 786478, i32 0, metadata !6, metadata !"instanceMethodCall", metadata !"instanceMethodCall", metadata !"", metadata !6, i32 7, metadata !7, i1 false, i1 true, i32 0, i32 0, null, i32 256, i1 false, void (%struct.Foo*)* @_Z18instanceMethodCallP3Foo, null, null, metadata !1, i32 7} ; [ DW_TAG_subprogram ] [line 7] [def] [instanceMethodCall]
!6 = metadata !{i32 786473, metadata !"methodCalls.cpp", metadata !"/Users/jondoe/IdeaProjects/SA/sanity/tests/res/cfg", null} ; [ DW_TAG_file_type ]
!7 = metadata !{i32 786453, i32 0, metadata !"", i32 0, i32 0, i64 0, i64 0, i64 0, i32 0, null, metadata !2, i32 0, i32 0} ; [ DW_TAG_subroutine_type ] [line 0, size 0, align 0, offset 0] [from ]
!8 = metadata !{i32 786478, i32 0, metadata !6, metadata !"staticMethodCall", metadata !"staticMethodCall", metadata !"", metadata !6, i32 11, metadata !7, i1 false, i1 true, i32 0, i32 0, null, i32 256, i1 false, void ()* @_Z16staticMethodCallv, null, null, metadata !1, i32 11} ; [ DW_TAG_subprogram ] [line 11] [def] [staticMethodCall]
!9 = metadata !{i32 786478, i32 0, metadata !6, metadata !"abstractMethodCall", metadata !"abstractMethodCall", metadata !"", metadata !6, i32 15, metadata !7, i1 false, i1 true, i32 0, i32 0, null, i32 256, i1 false, void (%struct.Foo*)* @_Z18abstractMethodCallP3Foo, null, null, metadata !1, i32 15} ; [ DW_TAG_subprogram ] [line 15] [def] [abstractMethodCall]
!10 = metadata !{i32 8, i32 0, metadata !5, null}
!11 = metadata !{i32 9, i32 0, metadata !5, null}
!12 = metadata !{i32 12, i32 0, metadata !8, null}
!13 = metadata !{i32 13, i32 0, metadata !8, null}
!14 = metadata !{i32 16, i32 0, metadata !9, null}
!15 = metadata !{i32 17, i32 0, metadata !9, null}
