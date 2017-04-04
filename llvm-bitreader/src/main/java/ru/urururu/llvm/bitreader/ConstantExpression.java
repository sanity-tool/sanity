package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.Expression;
import ru.urururu.llvm.bitreader.codes.FunctionCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ConstantExpression extends Value implements Expression {
    private final FunctionCodes code;
    private final Value[] operands;

    ConstantExpression(FunctionCodes code, Type type, Value[] operands) {
        super(type);
        this.code = code;
        this.operands = operands;
    }

    @Override
    public FunctionCodes getCode() {
        return code;
    }

    @Override
    public Value[] getOperands() {
        return operands;
    }
}
