// CheckStyle: start generated
package com.oracle.truffle.llvm.nodes.api;

import com.oracle.truffle.api.dsl.GeneratedBy;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.oracle.truffle.llvm.runtime.*;
import com.oracle.truffle.llvm.runtime.floating.LLVM80BitFloat;
import com.oracle.truffle.llvm.runtime.vector.*;

@GeneratedBy(LLVMTypes.class)
public final class LLVMTypesGen extends LLVMTypes {

    @Deprecated public static final LLVMTypesGen LLVMTYPES = new LLVMTypesGen();

    protected LLVMTypesGen() {
    }

    public static boolean isBoolean(Object value) {
        return value instanceof Boolean;
    }

    public static boolean asBoolean(Object value) {
        assert value instanceof Boolean : "LLVMTypesGen.asBoolean: boolean expected";
        return (boolean) value;
    }

    public static boolean expectBoolean(Object value) throws UnexpectedResultException {
        if (value instanceof Boolean) {
            return (boolean) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isByte(Object value) {
        return value instanceof Byte;
    }

    public static byte asByte(Object value) {
        assert value instanceof Byte : "LLVMTypesGen.asByte: byte expected";
        return (byte) value;
    }

    public static byte expectByte(Object value) throws UnexpectedResultException {
        if (value instanceof Byte) {
            return (byte) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isShort(Object value) {
        return value instanceof Short;
    }

    public static short asShort(Object value) {
        assert value instanceof Short : "LLVMTypesGen.asShort: short expected";
        return (short) value;
    }

    public static short expectShort(Object value) throws UnexpectedResultException {
        if (value instanceof Short) {
            return (short) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isInteger(Object value) {
        return value instanceof Integer;
    }

    public static int asInteger(Object value) {
        assert value instanceof Integer : "LLVMTypesGen.asInteger: int expected";
        return (int) value;
    }

    public static int expectInteger(Object value) throws UnexpectedResultException {
        if (value instanceof Integer) {
            return (int) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isCharacter(Object value) {
        return value instanceof Character;
    }

    public static char asCharacter(Object value) {
        assert value instanceof Character : "LLVMTypesGen.asCharacter: char expected";
        return (char) value;
    }

    public static char expectCharacter(Object value) throws UnexpectedResultException {
        if (value instanceof Character) {
            return (char) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLong(Object value) {
        return value instanceof Long;
    }

    public static long asLong(Object value) {
        assert value instanceof Long : "LLVMTypesGen.asLong: long expected";
        return (long) value;
    }

    public static long expectLong(Object value) throws UnexpectedResultException {
        if (value instanceof Long) {
            return (long) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isDouble(Object value) {
        return value instanceof Double;
    }

    public static double asDouble(Object value) {
        assert value instanceof Double : "LLVMTypesGen.asDouble: double expected";
        return (double) value;
    }

    public static double expectDouble(Object value) throws UnexpectedResultException {
        if (value instanceof Double) {
            return (double) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isFloat(Object value) {
        return value instanceof Float;
    }

    public static float asFloat(Object value) {
        assert value instanceof Float : "LLVMTypesGen.asFloat: float expected";
        return (float) value;
    }

    public static float expectFloat(Object value) throws UnexpectedResultException {
        if (value instanceof Float) {
            return (float) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isByteArray(Object value) {
        return value instanceof byte[];
    }

    public static byte[] asByteArray(Object value) {
        assert value instanceof byte[] : "LLVMTypesGen.asByteArray: byte[] expected";
        return (byte[]) value;
    }

    public static byte[] expectByteArray(Object value) throws UnexpectedResultException {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMI8Vector(Object value) {
        return value instanceof LLVMI8Vector;
    }

    public static LLVMI8Vector asLLVMI8Vector(Object value) {
        assert value instanceof LLVMI8Vector : "LLVMTypesGen.asLLVMI8Vector: LLVMI8Vector expected";
        return (LLVMI8Vector) value;
    }

    public static LLVMI8Vector expectLLVMI8Vector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMI8Vector) {
            return (LLVMI8Vector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMI64Vector(Object value) {
        return value instanceof LLVMI64Vector;
    }

    public static LLVMI64Vector asLLVMI64Vector(Object value) {
        assert value instanceof LLVMI64Vector : "LLVMTypesGen.asLLVMI64Vector: LLVMI64Vector expected";
        return (LLVMI64Vector) value;
    }

    public static LLVMI64Vector expectLLVMI64Vector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMI64Vector) {
            return (LLVMI64Vector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMI32Vector(Object value) {
        return value instanceof LLVMI32Vector;
    }

    public static LLVMI32Vector asLLVMI32Vector(Object value) {
        assert value instanceof LLVMI32Vector : "LLVMTypesGen.asLLVMI32Vector: LLVMI32Vector expected";
        return (LLVMI32Vector) value;
    }

    public static LLVMI32Vector expectLLVMI32Vector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMI32Vector) {
            return (LLVMI32Vector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMI1Vector(Object value) {
        return value instanceof LLVMI1Vector;
    }

    public static LLVMI1Vector asLLVMI1Vector(Object value) {
        assert value instanceof LLVMI1Vector : "LLVMTypesGen.asLLVMI1Vector: LLVMI1Vector expected";
        return (LLVMI1Vector) value;
    }

    public static LLVMI1Vector expectLLVMI1Vector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMI1Vector) {
            return (LLVMI1Vector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMI16Vector(Object value) {
        return value instanceof LLVMI16Vector;
    }

    public static LLVMI16Vector asLLVMI16Vector(Object value) {
        assert value instanceof LLVMI16Vector : "LLVMTypesGen.asLLVMI16Vector: LLVMI16Vector expected";
        return (LLVMI16Vector) value;
    }

    public static LLVMI16Vector expectLLVMI16Vector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMI16Vector) {
            return (LLVMI16Vector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMFloatVector(Object value) {
        return value instanceof LLVMFloatVector;
    }

    public static LLVMFloatVector asLLVMFloatVector(Object value) {
        assert value instanceof LLVMFloatVector : "LLVMTypesGen.asLLVMFloatVector: LLVMFloatVector expected";
        return (LLVMFloatVector) value;
    }

    public static LLVMFloatVector expectLLVMFloatVector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMFloatVector) {
            return (LLVMFloatVector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMDoubleVector(Object value) {
        return value instanceof LLVMDoubleVector;
    }

    public static LLVMDoubleVector asLLVMDoubleVector(Object value) {
        assert value instanceof LLVMDoubleVector : "LLVMTypesGen.asLLVMDoubleVector: LLVMDoubleVector expected";
        return (LLVMDoubleVector) value;
    }

    public static LLVMDoubleVector expectLLVMDoubleVector(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMDoubleVector) {
            return (LLVMDoubleVector) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMIVarBit(Object value) {
        return value instanceof LLVMIVarBit;
    }

    public static LLVMIVarBit asLLVMIVarBit(Object value) {
        assert value instanceof LLVMIVarBit : "LLVMTypesGen.asLLVMIVarBit: LLVMIVarBit expected";
        return (LLVMIVarBit) value;
    }

    public static LLVMIVarBit expectLLVMIVarBit(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMIVarBit) {
            return (LLVMIVarBit) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMTruffleAddress(Object value) {
        return value instanceof LLVMTruffleAddress;
    }

    public static LLVMTruffleAddress asLLVMTruffleAddress(Object value) {
        assert value instanceof LLVMTruffleAddress : "LLVMTypesGen.asLLVMTruffleAddress: LLVMTruffleAddress expected";
        return (LLVMTruffleAddress) value;
    }

    public static LLVMTruffleAddress expectLLVMTruffleAddress(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMTruffleAddress) {
            return (LLVMTruffleAddress) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMTruffleObject(Object value) {
        return value instanceof LLVMTruffleObject;
    }

    public static LLVMTruffleObject asLLVMTruffleObject(Object value) {
        assert value instanceof LLVMTruffleObject : "LLVMTypesGen.asLLVMTruffleObject: LLVMTruffleObject expected";
        return (LLVMTruffleObject) value;
    }

    public static LLVMTruffleObject expectLLVMTruffleObject(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMTruffleObject) {
            return (LLVMTruffleObject) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVM80BitFloat(Object value) {
        return value instanceof LLVM80BitFloat;
    }

    public static LLVM80BitFloat asLLVM80BitFloat(Object value) {
        assert value instanceof LLVM80BitFloat : "LLVMTypesGen.asLLVM80BitFloat: LLVM80BitFloat expected";
        return (LLVM80BitFloat) value;
    }

    public static LLVM80BitFloat expectLLVM80BitFloat(Object value) throws UnexpectedResultException {
        if (value instanceof LLVM80BitFloat) {
            return (LLVM80BitFloat) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMFunctionDescriptor(Object value) {
        return value instanceof LLVMFunctionDescriptor;
    }

    public static LLVMFunctionDescriptor asLLVMFunctionDescriptor(Object value) {
        assert value instanceof LLVMFunctionDescriptor : "LLVMTypesGen.asLLVMFunctionDescriptor: LLVMFunctionDescriptor expected";
        return (LLVMFunctionDescriptor) value;
    }

    public static LLVMFunctionDescriptor expectLLVMFunctionDescriptor(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMFunctionDescriptor) {
            return (LLVMFunctionDescriptor) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMAddress(Object value) {
        return value instanceof LLVMAddress;
    }

    public static LLVMAddress asLLVMAddress(Object value) {
        assert value instanceof LLVMAddress : "LLVMTypesGen.asLLVMAddress: LLVMAddress expected";
        return (LLVMAddress) value;
    }

    public static LLVMAddress expectLLVMAddress(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMAddress) {
            return (LLVMAddress) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isLLVMFunction(Object value) {
        return value instanceof LLVMFunction;
    }

    public static LLVMFunction asLLVMFunction(Object value) {
        assert value instanceof LLVMFunction : "LLVMTypesGen.asLLVMFunction: LLVMFunction expected";
        return (LLVMFunction) value;
    }

    public static LLVMFunction expectLLVMFunction(Object value) throws UnexpectedResultException {
        if (value instanceof LLVMFunction) {
            return (LLVMFunction) value;
        }
        throw new UnexpectedResultException(value);
    }

    public static boolean isTruffleObject(Object value) {
        return value instanceof TruffleObject;
    }

    public static TruffleObject asTruffleObject(Object value) {
        assert value instanceof TruffleObject : "LLVMTypesGen.asTruffleObject: TruffleObject expected";
        return (TruffleObject) value;
    }

    public static TruffleObject expectTruffleObject(Object value) throws UnexpectedResultException {
        if (value instanceof TruffleObject) {
            return (TruffleObject) value;
        }
        throw new UnexpectedResultException(value);
    }

}
