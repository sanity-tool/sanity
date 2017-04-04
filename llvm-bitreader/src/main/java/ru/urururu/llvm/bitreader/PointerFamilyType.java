package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class PointerFamilyType extends Type {
    private Type elementType;

    PointerFamilyType(TypeCodes code) {
        super(code);
    }

    public Type getElementType() {
        return elementType;
    }

    void setElementType(Type elementType) {
        if (elementType == null || elementType == this) {
            throw new IllegalArgumentException("elementType: " + elementType);
        }

        this.elementType = elementType;
    }
}
