package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class FunctionType extends Type {
    private final Type[] paramTypes;
    private final boolean vararg;
    private Type returnType;

    public FunctionType(Type[] paramTypes, TypeCodes code, boolean vararg) {
        super(code);
        this.paramTypes = paramTypes;
        this.vararg = vararg;

        for (Type paramType : paramTypes) {
            if (paramType == null) {
                throw new IllegalArgumentException("paramType: " + paramType);
            }
        }
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type[] getParamTypes() {
        return paramTypes;
    }

    void setReturnType(Type returnType) {
        if (returnType == this || returnType == null) {
            throw new IllegalArgumentException("returnType: " + returnType);
        }
        this.returnType = returnType;
    }

    public boolean isVararg() {
        return vararg;
    }
}
