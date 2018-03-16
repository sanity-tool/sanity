package ru.urururu.sanity.cpp;

import io.swagger.client.model.TypeDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.TypeParser;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.FinalMap;
import ru.urururu.util.Iterables;

import java.util.*;
import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteTypeParser extends TypeParser<TypeDto> {
    private final Map<String, Function<TypeDto, Type>> parsers = FinalMap.createHashMap();

    public RemoteTypeParser() {
        parsers.put("LLVMVoidTypeKind", t -> createVoid());
        // half-type?
        parsers.put("LLVMFloatTypeKind", t -> createFloat());
        parsers.put("LLVMDoubleTypeKind", t -> createDouble());
        parsers.put("LLVMX86_FP80TypeKind", t -> createLongDouble());
        parsers.put("LLVMIntegerTypeKind", t -> createInt(bitreader.LLVMGetIntTypeWidth(t)));
        parsers.put("LLVMFunctionTypeKind", this::parseFunction);
        parsers.put("LLVMStructTypeKind", this::parseStruct);
        parsers.put("LLVMArrayTypeKind",
                t -> createArray(bitreader.LLVMGetElementType(t), bitreader.LLVMGetArrayLength(t)));
        parsers.put("LLVMPointerTypeKind", t -> createPointer(bitreader.LLVMGetElementType(t)));
        parsers.put("LLVMMetadataTypeKind", t -> createMetadata());
    }

    public Type parse(TypeDto type) {
        String typeKind = bitreader.LLVMGetTypeKind(type);
        return typesCache.computeIfAbsent(type, key -> parsers.getOrDefault(typeKind, this::parseUnknown).apply(type));
    }

    private Type parseFunction(TypeDto type) {
        int params = (int) bitreader.LLVMCountParamTypes(type);
        if (params != 0) {
            SWIGTYPE_p_p_LLVMOpaqueType paramsBuff = bitreader.calloc_LLVMTypeRef(params, bitreader.sizeof_LLVMTypeRef);
            try {
                bitreader.LLVMGetParamTypes(type, paramsBuff);
                return createFunction(bitreader.LLVMGetReturnType(type),
                        Iterables.indexed(i -> bitreader.getType(paramsBuff, i), () -> params));
            } finally {
                bitreader.free_LLVMTypeRef(paramsBuff);
            }
        }

        return createFunction(bitreader.LLVMGetReturnType(type), Collections.emptyList());
    }

    private Type parseStruct(TypeDto type) {
        int fields = (int) bitreader.LLVMCountStructElementTypes(type);
        SWIGTYPE_p_p_LLVMOpaqueType fieldsBuff = bitreader.calloc_LLVMTypeRef(fields, bitreaderConstants.sizeof_LLVMTypeRef);
        try {
            bitreader.LLVMGetStructElementTypes(type, fieldsBuff);
            return createStruct(type, type.getName(), type.getTypes() Iterables.indexed(i -> bitreader.getType(fieldsBuff, i), () -> fields));
        } finally {
            bitreader.free_LLVMTypeRef(fieldsBuff);
        }
    }

    private Type parseUnknown(TypeDto type) {
        String typeKind = type.getKind();
        throw new IllegalStateException("Can't parse " + typeKind);
    }
}
