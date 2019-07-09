package ru.urururu.sanity.cpp;

import io.swagger.client.model.ModuleDto;
import io.swagger.client.model.TypeDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.TypeParser;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.util.FinalMap;

import java.util.*;
import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteTypeParser extends TypeParser<Integer> implements ParserListener {
    private final Map<String, Function<Integer, Type>> parsers = FinalMap.createHashMap();
    private ModuleDto currentModule;

    public RemoteTypeParser() {
        parsers.put("LLVMVoidTypeKind", t -> createVoid());
        // half-type?
        parsers.put("LLVMFloatTypeKind", t -> createFloat());
        parsers.put("LLVMDoubleTypeKind", t -> createDouble());
        parsers.put("LLVMX86_FP80TypeKind", t -> createLongDouble());
        parsers.put("LLVMIntegerTypeKind", t -> createInt(currentModule.getTypes().get(t).getLen()));
        parsers.put("LLVMFunctionTypeKind", this::parseFunction);
        parsers.put("LLVMStructTypeKind", this::parseStruct);
        parsers.put("LLVMArrayTypeKind",
                t -> createArray(currentModule.getTypes().get(t).getTypes().get(0), currentModule.getTypes().get(t).getLen()));
        parsers.put("LLVMPointerTypeKind", t -> createPointer(currentModule.getTypes().get(t).getTypes().get(0)));
        parsers.put("LLVMMetadataTypeKind", t -> createMetadata());
    }

    public Type parse(Integer type) {
        TypeDto typeDto = currentModule.getTypes().get(type);
        String typeKind = typeDto.getKind();
        return typesCache.computeIfAbsent(type, key -> parsers.getOrDefault(typeKind, this::parseUnknown).apply(type));
    }

    private Type parseFunction(Integer type) {
        TypeDto typeDto = currentModule.getTypes().get(type);
        List<Integer> types = typeDto.getTypes();
        return createFunction(types.get(0), types.subList(1, types.size()));
    }

    private Type parseStruct(Integer type) {
        TypeDto typeDto = currentModule.getTypes().get(type);
        String name = parsers.getSettings().maskTypename(typeDto.getName());
        return createStruct(type, name, typeDto.getTypes());
    }

    private Type parseUnknown(Integer type) {
        TypeDto typeDto = currentModule.getTypes().get(type);
        String typeKind = typeDto.getKind();
        throw new IllegalStateException("Can't parse " + typeKind);
    }

    @Override
    public void onModuleStarted(ModuleDto module) {
        currentModule = module;
    }

    @Override
    public void onModuleFinished(ModuleDto module) {
        currentModule = null;
        typesCache.clear();
        structCache.clear();
    }
}
