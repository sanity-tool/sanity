package ru.urururu.llvm.bitreader.codes;

import ru.urururu.llvm.bitreader.BlockId;

/**
 * TYPE blocks have codes for each type primitive they use.
 *
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public enum TypeCodes implements Codes {
    /**
     * NUMENTRY: [numentries]
     */
    TYPE_CODE_NUMENTRY(1),

    // Type Codes
    /**
     * VOID
     */
    TYPE_CODE_VOID(2),
    /**
     * FLOAT
     */
    TYPE_CODE_FLOAT(3),
    /**
     * DOUBLE
     */
    TYPE_CODE_DOUBLE(4),
    /**
     * LABEL
     */
    TYPE_CODE_LABEL(5),
    /**
     * OPAQUE
     */
    TYPE_CODE_OPAQUE(6),
    /**
     * INTEGER: [width]
     */
    TYPE_CODE_INTEGER(7),

    /**
     * POINTER: [pointee type]
     */
    TYPE_CODE_POINTER(8),

    /**
     * FUNCTION: [vararg, attrid, retty, paramty x N]
     */
    TYPE_CODE_FUNCTION_OLD(9),

    /**
     * HALF
     */
    TYPE_CODE_HALF(10),

    /**
     * ARRAY: [numelts, eltty]
     */
    TYPE_CODE_ARRAY(11),

    /**
     * VECTOR: [numelts, eltty]
     */
    TYPE_CODE_VECTOR(12),

    // These are not with the other floating point types because they're
    // a late addition, and putting them in the right place breaks
    // binary compatibility.

    /**
     * X86 LONG DOUBLE
     */
    TYPE_CODE_X86_FP80(13),

    /**
     * LONG DOUBLE (112 bit mantissa)
     */
    TYPE_CODE_FP128(14),

    /**
     * PPC LONG DOUBLE (2 doubles)
     */
    TYPE_CODE_PPC_FP128(15),

    /**
     * METADATA
     */
    TYPE_CODE_METADATA(16),

    /**
     * X86 MMX
     */
    TYPE_CODE_X86_MMX(17),

    /**
     * STRUCT_ANON: [ispacked, eltty x N]
     */
    TYPE_CODE_STRUCT_ANON(18),

    /**
     * STRUCT_NAME: [strchr x N]
     */
    TYPE_CODE_STRUCT_NAME(19),

    /**
     * STRUCT_NAMED: [ispacked, eltty x N]
     */
    TYPE_CODE_STRUCT_NAMED(20),

    /**
     * FUNCTION: [vararg, retty, paramty x N]
     */
    TYPE_CODE_FUNCTION(21),

    /**
     * TOKEN
     */
    TYPE_CODE_TOKEN(22),
    ;

    TypeCodes(int code) {
        register(BlockId.TYPE_BLOCK, code, this);
    }
};
