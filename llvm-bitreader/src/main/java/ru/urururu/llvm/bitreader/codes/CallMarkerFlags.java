package ru.urururu.llvm.bitreader.codes;

/**
 * Markers and flags for call instruction.
 *
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CallMarkerFlags {
    public static final int CALL_TAIL = 0;
    public static final int CALL_CCONV = 1;
    public static final int CALL_MUSTTAIL = 14;
    public static final int CALL_EXPLICIT_TYPE = 15;
    public static final int CALL_NOTAIL = 16;
    /**
     * Call has optional fast-math-flags.
     */
    public static final int CALL_FMF = 17;
}
