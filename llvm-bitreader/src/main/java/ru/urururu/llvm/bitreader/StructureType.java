package ru.urururu.llvm.bitreader;

import ru.urururu.llvm.bitreader.codes.TypeCodes;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class StructureType extends Type {
    private final String typeName;
    private final List<Type> fieldTypes;

    public StructureType(String typeName, List<Type> fieldTypes, TypeCodes code) {
        super(code);
        this.typeName = typeName;
        this.fieldTypes = fieldTypes;
    }

    public String getTypeName() {
        return typeName;
    }

    public List<Type> getFieldTypes() {
        return fieldTypes;
    }
}
