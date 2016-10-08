package ru.urururu.sanity.cpp;

import ru.urururu.sanity.api.cfg.ArrayType;
import ru.urururu.sanity.api.cfg.PointerType;
import ru.urururu.sanity.api.cfg.Primitive;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.sanity.cpp.llvm.LLVMTypeKind;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.bitreader;
import ru.urururu.sanity.cpp.llvm.bitreaderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class TypeParser implements ParserListener {
    private final Map<LLVMTypeKind, TypeKindParser> parsers;

    private Map<SWIGTYPE_p_LLVMOpaqueType, Type> typesCache;
    private Map<SWIGTYPE_p_LLVMOpaqueType, Type> structCache;

    private static final TypeKindParser defaultParser = new TypeKindParser() {
        @Override
        public LLVMTypeKind getTypeKind() {
            return null;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
            throw new IllegalStateException("Can't parse " + typeKind);
        }
    };

    @Autowired
    public TypeParser(TypeKindParser[] parsers) {
        this.parsers = new HashMap<>();

        for (TypeKindParser parser : parsers) {
            this.parsers.put(parser.getTypeKind(), parser);
        }
    }

    public Type parse(SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        return typesCache.computeIfAbsent(type, key -> parsers.getOrDefault(typeKind, defaultParser).parse(this, key));
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module) {
        structCache = new HashMap<>();
        typesCache = new HashMap<>();
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
        structCache = null;
    }

    private Type get(SWIGTYPE_p_LLVMOpaqueType type) {
        return structCache.get(type);
    }

    private void cache(SWIGTYPE_p_LLVMOpaqueType type, Type struct) {
        structCache.put(type, struct);
    }

    private interface TypeKindParser {
        LLVMTypeKind getTypeKind();

        Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type);
    }

    @Component
    private static class IntegerParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMIntegerTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new Primitive("int" + bitreader.LLVMGetIntTypeWidth(type));
        }
    }

    @Component
    private static class FloatParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMFloatTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new Primitive("float");
        }
    }

    @Component
    private static class DoubleParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMDoubleTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new Primitive("double");
        }
    }

    @Component
    private static class FP80Parser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMX86_FP80TypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new Primitive("long double");
        }
    }

    @Component
    private static class VoidParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMVoidTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new Primitive("void");
        }
    }

    @Component
    private static class PointerParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMPointerTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new PointerType(typeParser.parse(bitreader.LLVMGetElementType(type)));
        }
    }

    @Component
    private static class ArrayParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMArrayTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            return new ArrayType(typeParser.parse(bitreader.LLVMGetElementType(type)), bitreader.LLVMGetArrayLength(type));
        }
    }

    @Component
    private static class StructParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMStructTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            Type cached = typeParser.get(type);
            if (cached != null) {
                return cached;
            }

            String name = bitreader.LLVMGetStructName(type);
            int fields = (int)bitreader.LLVMCountStructElementTypes(type);
            SWIGTYPE_p_p_LLVMOpaqueType fieldsBuff = bitreader.calloc_LLVMTypeRef(fields, bitreaderConstants.sizeof_LLVMTypeRef);
            try {
                bitreader.LLVMGetStructElementTypes(type, fieldsBuff);

                final List<Type> fieldTypes = new ArrayList<>();

                Type struct = new Type() {
                    @Override
                    public Type getElementType() {
                        return null;
                    }

                    @Override
                    public Type getFieldType(int index) {
                        if (index >= fieldTypes.size()) {
                            throw new IllegalStateException("bad");
                        }
                        return fieldTypes.get(index);
                    }

                    @Override
                    public String getFieldName(int index) {
                        return "field" + index;
                    }

                    @Override
                    public String toString() {
                        return name;
                    }
                };

                typeParser.cache(type, struct);

                for (int i = 0; i < fields; i++) {
                    Type fieldType = typeParser.parse(bitreader.getType(fieldsBuff, i));
                    fieldTypes.add(fieldType);
                }

                return struct;
            } finally {
                bitreader.free_LLVMTypeRef(fieldsBuff);
            }
        }
    }

    @Component
    private static class FunctionParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMFunctionTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            int params = (int) bitreader.LLVMCountParamTypes(type);
            final Type returnType = typeParser.parse(bitreader.LLVMGetReturnType(type));
            final Type[] paramsType = new Type[params];
            if (params != 0) {
                SWIGTYPE_p_p_LLVMOpaqueType paramsBuff = bitreader.calloc_LLVMTypeRef(params, bitreader.sizeof_LLVMTypeRef);
                try {
                    bitreader.LLVMGetParamTypes(type, paramsBuff);
                    for (int i = 0; i < params; i++) {
                        paramsType[i] = typeParser.parse(bitreader.getType(paramsBuff, i));
                    }
                } finally {
                    bitreader.free_LLVMTypeRef(paramsBuff);
                }
            }

            return new Type() {
                @Override
                public Type getElementType() {
                    return null;
                }

                @Override
                public Type getFieldType(int index) {
                    return null;
                }

                @Override
                public String getFieldName(int index) {
                    return null;
                }

                @Override
                public String toString() {
                    StringBuilder sb = new StringBuilder(returnType.toString());
                    sb.append(" (");
                    if (paramsType.length != 0) {
                        sb.append(paramsType[0]);
                        for (int i = 1; i < paramsType.length; i++) {
                            sb.append(", ").append(paramsType[i]);
                        }
                    }
                    sb.append(')');
                    return sb.toString();
                }
            };
        }
    }

}
