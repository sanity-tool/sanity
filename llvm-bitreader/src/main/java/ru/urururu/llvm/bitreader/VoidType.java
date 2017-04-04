package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class VoidType extends Type {
    public static final VoidType INSTANCE = new VoidType(TypeCodes.TYPE_CODE_VOID);

    private VoidType(TypeCodes code) {
        super(code);
    }
}
