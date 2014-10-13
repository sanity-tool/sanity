package na.okutane.cpp;

import na.okutane.api.cfg.PointerType;
import na.okutane.api.cfg.Type;
import na.okutane.cpp.llvm.LLVMTypeKind;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import na.okutane.cpp.llvm.SWIGTYPE_p_p_LLVMOpaqueType;
import na.okutane.cpp.llvm.bitreader;
import na.okutane.cpp.llvm.bitreaderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class TypeParser {
    private final Map<LLVMTypeKind, TypeKindParser> parsers;

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
        this.parsers = new HashMap<LLVMTypeKind, TypeKindParser>();

        for (TypeKindParser parser : parsers) {
            this.parsers.put(parser.getTypeKind(), parser);
        }
    }

    public Type parse(SWIGTYPE_p_LLVMOpaqueType type) {
        LLVMTypeKind typeKind = bitreader.LLVMGetTypeKind(type);
        return parsers.getOrDefault(typeKind, defaultParser).parse(this, type);
    }

    private static interface TypeKindParser {
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
            return new Type() {
                @Override
                public Type getElementType() {
                    return null;
                }

                @Override
                public Type getFieldType(int index) {
                    return null;
                }
            };
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
    private static class StructParser implements TypeKindParser {
        @Override
        public LLVMTypeKind getTypeKind() {
            return LLVMTypeKind.LLVMStructTypeKind;
        }

        @Override
        public Type parse(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueType type) {
            int fields = (int)bitreader.LLVMCountStructElementTypes(type);
            SWIGTYPE_p_p_LLVMOpaqueType fieldsBuff = bitreader.calloc_LLVMTypeRef(fields, bitreaderConstants.sizeof_LLVMTypeRef);
            try {
                bitreader.LLVMGetStructElementTypes(type, fieldsBuff);

                final List<Type> fieldTypes = new ArrayList<Type>();
                for (int i = 0; i < fields; i++) {
                    Type fieldType = typeParser.parse(bitreader.getType(fieldsBuff, i));
                    fieldTypes.add(fieldType);
                }

                return new Type() {
                    @Override
                    public Type getElementType() {
                        return null;
                    }

                    @Override
                    public Type getFieldType(int index) {
                        return fieldTypes.get(index);
                    }
                };
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
            return new Type() {
                @Override
                public Type getElementType() {
                    return null;
                }

                @Override
                public Type getFieldType(int index) {
                    return null;
                }
            };
        }
    }
}
