package na.okutane.cpp;

import na.okutane.api.cfg.PointerType;
import na.okutane.api.cfg.Type;
import na.okutane.cpp.llvm.LLVMTypeKind;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.SWIGTYPE_p_p_LLVMOpaqueType;
import na.okutane.cpp.llvm.SWIGTYPE_p_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import na.okutane.cpp.llvm.bitreaderConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class TypeParser implements ParserListener {
    public static final int DW_TAG_member = 786445;
    public static final int DW_TAG_structure_type = 786451;
    public static final int DW_TAG_union_type = 786455;
    public static final int DW_TAG_compile_unit = 786449;
    private final Map<LLVMTypeKind, TypeKindParser> parsers;

    private Map<String, List<String>> fieldNamesCache;
    private int anonCount;
    private Set<SWIGTYPE_p_LLVMOpaqueValue> visited;

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
        return parsers.getOrDefault(typeKind, defaultParser).parse(this, type);
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module) {
        fieldNamesCache = new HashMap<>();
        visited = new HashSet<>();
        anonCount = 0;

        int arguments = (int) bitreader.LLVMGetNamedMetadataNumOperands(module, "llvm.dbg.cu");
        SWIGTYPE_p_p_LLVMOpaqueValue values = bitreader.calloc_LLVMValueRef(arguments, bitreaderConstants.sizeof_LLVMValueRef);
        try {
            bitreader.LLVMGetNamedMetadataOperands(module, "llvm.dbg.cu", values);

            for (int i = 0; i < arguments; i++) {
                // process compile units
                SWIGTYPE_p_LLVMOpaqueValue compileUnitMD = bitreader.getValue(values, i);
                if (checkTag(compileUnitMD, DW_TAG_compile_unit)) {
                    visit(compileUnitMD);
                } else {
                    throw new IllegalStateException("not a compilation unit: " + bitreader.LLVMPrintValueToString(compileUnitMD));
                }
            }
        } finally {
            bitreader.free_LLVMValueRef(values);
        }
    }

    private static boolean checkTag(SWIGTYPE_p_LLVMOpaqueValue node, long tag) {
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return false;
        }
        SWIGTYPE_p_LLVMOpaqueValue maybeTag = bitreader.LLVMGetOperand(node, 0);
        if (bitreader.LLVMIsAConstantInt(maybeTag) != null) {
            long val = bitreader.LLVMConstIntGetSExtValue(maybeTag);
            return val == tag;
        }
        return false;
    }

    protected void visitStructure(SWIGTYPE_p_LLVMOpaqueValue type) {
        if (!visited.add(type)) {
            return;
        }

        SWIGTYPE_p_LLVMOpaqueValue members = bitreader.LLVMGetOperand(type, 10);

        if (members == null) {
            return;
        }

        String name = bitreader.getMDString(bitreader.LLVMGetOperand(type, 3));

        if (name.equals("")) {
            if (anonCount == 0) {
                name = "anon";
            } else {
                name = "anon." + anonCount;
            }
            anonCount++;
        }

        String typeName = "struct." + name;

        List<String> fieldNames = new ArrayList<String>();

        for (int i = 0; i < bitreader.LLVMGetNumOperands(members); i++) {
            SWIGTYPE_p_LLVMOpaqueValue node = bitreader.LLVMGetOperand(members, i);
            if (checkTag(node, DW_TAG_member)) {
                String fieldName = bitreader.getMDString(bitreader.LLVMGetOperand(node, 3));
                fieldNames.add(fieldName);
            }
        }

        if (fieldNamesCache.put(typeName, fieldNames) != null) {
            throw new IllegalStateException("Fields already stored for " + typeName);
        }
    }

    protected void visit(SWIGTYPE_p_LLVMOpaqueValue node) {
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return;
        }

        if (checkTag(node, DW_TAG_structure_type)) {
            visitStructure(node);
            return;
        }

        for (int j = 0; j < bitreader.LLVMGetNumOperands(node); j++) {
            SWIGTYPE_p_LLVMOpaqueValue type = bitreader.LLVMGetOperand(node, j);
            visit(type);
        }
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
        fieldNamesCache = null;
        visited = null;
    }

    List<String> getFieldNames(SWIGTYPE_p_LLVMOpaqueType type) {
        String typeName = bitreader.LLVMGetStructName(type);
        return fieldNamesCache.get(typeName);
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

                @Override
                public String getFieldName(int index) {
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

                // DW_TAG_structure_type

                final List<Type> fieldTypes = new ArrayList<Type>();
                for (int i = 0; i < fields; i++) {
                    Type fieldType = typeParser.parse(bitreader.getType(fieldsBuff, i));
                    fieldTypes.add(fieldType);
                }

                final List<String> fieldNames = typeParser.getFieldNames(type);

                return new Type() {
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
                        if (fieldNames != null) {
                            return fieldNames.get(index);
                        }
                        return "field" + index;
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

                @Override
                public String getFieldName(int index) {
                    return null;
                }
            };
        }
    }
}
