package ru.urururu.llvm.bitreader;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import ru.urururu.llvm.bitreader.codes.Expression;
import ru.urururu.llvm.bitreader.codes.FunctionCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Instruction extends Value implements Expression {
    private final FunctionCodes code;
    private final Value[] operands;
    private Instruction next;
    private DebugLoc debugLoc;

    Instruction(FunctionCodes code, Type type, Value... operands) {
        super(type);
        this.code = code;
        this.operands = operands;

        if (code == FunctionCodes.FUNC_CODE_INST_STORE && operands[1] instanceof IntegerValue) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public FunctionCodes getCode() {
        return code;
    }

    public Value[] getOperands() {
        return operands;
    }

    void setNext(Instruction next) {
        this.next = next;
    }

    public Instruction getNext() {
        return next;
    }

    void setDebugLoc(DebugLoc debugLoc) {
        this.debugLoc = debugLoc;
    }

    public DebugLoc getDebugLoc() {
        return debugLoc;
    }
}
