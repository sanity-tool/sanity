package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ArrayType extends PointerFamilyType {
    private final int size;

    ArrayType(TypeCodes code, int size) {
        super(code);
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
