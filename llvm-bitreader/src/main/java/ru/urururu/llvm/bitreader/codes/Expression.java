package ru.urururu.llvm.bitreader.codes;

import ru.urururu.llvm.bitreader.Value;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Expression {
    public FunctionCodes getCode();

    Value[] getOperands();
}
