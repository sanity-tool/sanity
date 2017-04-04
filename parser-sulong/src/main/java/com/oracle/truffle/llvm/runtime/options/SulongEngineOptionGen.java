package com.oracle.truffle.llvm.runtime.options;
import com.oracle.truffle.llvm.option.OptionSummary;

public final class SulongEngineOptionGen extends SulongEngineOption {
  static {
    OptionSummary.registerOption("Base Options", "llvmVersion", "sulong.LLVM", String.valueOf(LLVM_VERSION), "Version of the used LLVM File Format, e.g., 3.2 (default) or 3.8.");
    OptionSummary.registerOption("Base Options", "nodeConfiguration", "sulong.NodeConfiguration", String.valueOf(NODE_CONFIGURATION), "The node configuration (node factory) to be used in Sulong.");
    OptionSummary.registerOption("Base Options", "stackSize", "sulong.StackSizeKB", String.valueOf(STACK_SIZE_KB), "The stack size in KB.");
    OptionSummary.registerOption("Base Options", "dynamicNativeLibraryPath", "sulong.DynamicNativeLibraryPath", java.util.Arrays.toString(DYN_LIBRARY_PATHS), "The native library search paths delimited by : .");
    OptionSummary.registerOption("Base Options", "dynamicBitcodeLibraries", "sulong.DynamicBitcodeLibraries", java.util.Arrays.toString(DYN_BITCODE_LIBRARIES), "The paths to shared bitcode libraries delimited by : .");
    OptionSummary.registerOption("Base Options", "projectRoot", "sulong.ProjectRoot", String.valueOf(PROJECT_ROOT), "Overrides the root of the project. This option exists to set the project root from mx.");
    OptionSummary.registerOption("Base Options", "executionCount", "sulong.ExecutionCount", String.valueOf(EXECUTION_COUNT), "Execute each program for as many times as specified by this option.");
    OptionSummary.registerOption("Base Options", "disableNativeInterface", "sulong.DisableNativeInterface", String.valueOf(DISABLE_NFI), "Disables Sulongs native interface.");
  }
  private final String internal_llvmVersion;
  private final String internal_nodeConfiguration;
  private final int internal_stackSize;
  private final String[] internal_dynamicNativeLibraryPath;
  private final String[] internal_dynamicBitcodeLibraries;
  private final String internal_projectRoot;
  private final int internal_executionCount;
  private final boolean internal_disableNativeInterface;
  private SulongEngineOptionGen() {
    internal_llvmVersion = getStringOption("sulong.LLVM", LLVM_VERSION);
    internal_nodeConfiguration = getStringOption("sulong.NodeConfiguration", NODE_CONFIGURATION);
    internal_stackSize = getIntegerOption("sulong.StackSizeKB", STACK_SIZE_KB);
    internal_dynamicNativeLibraryPath = getStringArrOption("sulong.DynamicNativeLibraryPath", DYN_LIBRARY_PATHS);
    internal_dynamicBitcodeLibraries = getStringArrOption("sulong.DynamicBitcodeLibraries", DYN_BITCODE_LIBRARIES);
    internal_projectRoot = getStringOption("sulong.ProjectRoot", PROJECT_ROOT);
    internal_executionCount = getIntegerOption("sulong.ExecutionCount", EXECUTION_COUNT);
    internal_disableNativeInterface = getBooleanOption("sulong.DisableNativeInterface", DISABLE_NFI);
  }
  public static SulongEngineOptionGen create() {
    return new SulongEngineOptionGen();
  }
  public String llvmVersion() {
    return internal_llvmVersion;
  }
  public String nodeConfiguration() {
    return internal_nodeConfiguration;
  }
  public int stackSize() {
    return internal_stackSize;
  }
  public String[] dynamicNativeLibraryPath() {
    return internal_dynamicNativeLibraryPath;
  }
  public String[] dynamicBitcodeLibraries() {
    return internal_dynamicBitcodeLibraries;
  }
  public String projectRoot() {
    return internal_projectRoot;
  }
  public int executionCount() {
    return internal_executionCount;
  }
  public boolean disableNativeInterface() {
    return internal_disableNativeInterface;
  }
  private static String[] getStringArrOption(String option, String[] defaultValue) {
    if (System.getProperty(option) == null) {
      return defaultValue;
    } else {
      return System.getProperty(option).split(":");
    }
  }

  private static int getIntegerOption(String option, int defaultValue) {
    if (System.getProperty(option) == null) {
      return defaultValue;
    } else {
      return Integer.getInteger(option);
    }
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
