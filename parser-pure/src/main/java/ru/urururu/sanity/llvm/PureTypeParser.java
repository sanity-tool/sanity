package ru.urururu.sanity.llvm;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.TypeParser;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureTypeParser extends TypeParser<Module, Type> {
    @Override
    public ru.urururu.sanity.api.cfg.Type parse(Type type) {
        switch (type.getCode()) {
            case TYPE_CODE_VOID:
                return createVoid();
            case TYPE_CODE_FLOAT:
                return createFloat();
            case TYPE_CODE_DOUBLE:
                return createDouble();
            case TYPE_CODE_OPAQUE:
                OpaqueType opaqueType = (OpaqueType) type;
                return createStruct(type, opaqueType.getTypeName(), Collections.emptyList());
            case TYPE_CODE_INTEGER:
                IntegerType integerType = (IntegerType) type;
                return createInt(integerType.getWidth());
            case TYPE_CODE_POINTER:
                return createPointer(((PointerType)type).getElementType());
            case TYPE_CODE_FUNCTION_OLD:
                break;
            case TYPE_CODE_HALF:
                break;
            case TYPE_CODE_ARRAY:
                ArrayType arrayType = (ArrayType) type;
                return createArray(arrayType.getElementType(), arrayType.getSize());
            case TYPE_CODE_VECTOR:
                break;
            case TYPE_CODE_X86_FP80:
                return createLongDouble();
            case TYPE_CODE_FP128:
                break;
            case TYPE_CODE_PPC_FP128:
                break;
            case TYPE_CODE_METADATA:
                return createMetadata();
            case TYPE_CODE_X86_MMX:
                break;
            case TYPE_CODE_STRUCT_ANON:
                break;
            case TYPE_CODE_STRUCT_NAME:
                break;
            case TYPE_CODE_STRUCT_NAMED:
                StructureType structureType = (StructureType) type;
                return createStruct(type, structureType.getTypeName(), structureType.getFieldTypes());
            case TYPE_CODE_FUNCTION:
                FunctionType functionType = (FunctionType) type;
                return createFunction(functionType.getReturnType(), Arrays.asList(functionType.getParamTypes()));
            case TYPE_CODE_TOKEN:
                break;
        }

        throw new NotImplementedException(type.getCode().name());
    }
}
