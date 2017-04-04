package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class PointerType extends PointerFamilyType {
    PointerType(TypeCodes code) {
        super(code);
    }
}
