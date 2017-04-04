package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class OpaqueType extends Type {
    private final String typeName;

    public OpaqueType(String typeName, TypeCodes code) {
        super(code);
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }
}
