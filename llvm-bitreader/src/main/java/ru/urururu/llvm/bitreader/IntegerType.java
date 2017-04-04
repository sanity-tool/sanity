package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class IntegerType extends Type {
    private final int width;

    IntegerType(int width, TypeCodes code) {
        super(code);
        this.width = width;
    }

    public int getWidth() {
        return width;
    }
}
