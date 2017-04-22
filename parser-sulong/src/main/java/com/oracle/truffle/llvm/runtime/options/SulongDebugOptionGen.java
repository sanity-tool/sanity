package com.oracle.truffle.llvm.runtime.options;
import com.oracle.truffle.llvm.option.OptionSummary;

public final class SulongDebugOptionGen extends SulongDebugOption {
  static {
    OptionSummary.registerOption("Debug Options", "debug", "sulong.Debug", String.valueOf(DEBUG), "Turns debugging on/off. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "verbose", "sulong.Verbose", String.valueOf(VERBOSE), "Enables verbose printing of debugging information. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "performanceWarningsAreFatal", "sulong.PerformanceWarningsAreFatal", String.valueOf(PERFORMANCE_WARNING_ARE_FATAL), "Terminates the program after a performance issue is encountered.");
    OptionSummary.registerOption("Debug Options", "tracePerformanceWarnings", "sulong.TracePerformanceWarnings", String.valueOf(PERFORMANCE_WARNINGS), "Reports all LLVMPerformance.warn() invokations in compiled code.");
    OptionSummary.registerOption("Debug Options", "printFunctionASTs", "sulong.PrintASTs", String.valueOf(PRINT_FUNCTION_ASTS), "Prints the Truffle ASTs for the parsed functions. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "printExecutionTime", "sulong.PrintExecutionTime", String.valueOf(PRINT_EXECUTION_TIME), "Prints the execution time for the main function of the program. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "printNativeCallStatistics", "sulong.PrintNativeCallStats", String.valueOf(NATIVE_CALL_STATS), "Outputs stats about native call site frequencies. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "printLifetimeAnalysisStatistics", "sulong.PrintLifetimeAnalysisStats", String.valueOf(PRINT_LIFE_TIME_ANALYSIS_STATS), "Prints the results of the lifetime analysis. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
    OptionSummary.registerOption("Debug Options", "traceExecution", "sulong.TraceExecution", String.valueOf(TRACE_EXECUTION), "Trace execution, printing each SSA assignment. Can be 'true', 'false', 'stdout', 'stderr' or a filepath.");
  }
  private final String internal_debug;
  private final String internal_verbose;
  private final boolean internal_performanceWarningsAreFatal;
  private final boolean internal_tracePerformanceWarnings;
  private final String internal_printFunctionASTs;
  private final String internal_printExecutionTime;
  private final String internal_printNativeCallStatistics;
  private final String internal_printLifetimeAnalysisStatistics;
  private final String internal_traceExecution;
  private SulongDebugOptionGen() {
    internal_debug = getStringOption("sulong.Debug", DEBUG);
    internal_verbose = getStringOption("sulong.Verbose", VERBOSE);
    internal_performanceWarningsAreFatal = getBooleanOption("sulong.PerformanceWarningsAreFatal", PERFORMANCE_WARNING_ARE_FATAL);
    internal_tracePerformanceWarnings = getBooleanOption("sulong.TracePerformanceWarnings", PERFORMANCE_WARNINGS);
    internal_printFunctionASTs = getStringOption("sulong.PrintASTs", PRINT_FUNCTION_ASTS);
    internal_printExecutionTime = getStringOption("sulong.PrintExecutionTime", PRINT_EXECUTION_TIME);
    internal_printNativeCallStatistics = getStringOption("sulong.PrintNativeCallStats", NATIVE_CALL_STATS);
    internal_printLifetimeAnalysisStatistics = getStringOption("sulong.PrintLifetimeAnalysisStats", PRINT_LIFE_TIME_ANALYSIS_STATS);
    internal_traceExecution = getStringOption("sulong.TraceExecution", TRACE_EXECUTION);
  }
  public static SulongDebugOptionGen create() {
    return new SulongDebugOptionGen();
  }
  public String debug() {
    return internal_debug;
  }
  public String verbose() {
    return internal_verbose;
  }
  public boolean performanceWarningsAreFatal() {
    return internal_performanceWarningsAreFatal;
  }
  public boolean tracePerformanceWarnings() {
    return internal_tracePerformanceWarnings;
  }
  public String printFunctionASTs() {
    return internal_printFunctionASTs;
  }
  public String printExecutionTime() {
    return internal_printExecutionTime;
  }
  public String printNativeCallStatistics() {
    return internal_printNativeCallStatistics;
  }
  public String printLifetimeAnalysisStatistics() {
    return internal_printLifetimeAnalysisStatistics;
  }
  public String traceExecution() {
    return internal_traceExecution;
  }
  private static boolean getBooleanOption(String option, boolean defaultValue) {
    if (System.getProperty(option) == null) {
      return defaultValue;
    } else {
      return Boolean.getBoolean(option);
    }
  }

  private static String getStringOption(String option, String defaultValue) {
    if (System.getProperty(option) == null) {
      return defaultValue;
    } else {
      return System.getProperty(option);
    }
  }

}
