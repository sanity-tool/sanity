package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.ArrayType;
import ru.urururu.sanity.api.cfg.PointerType;
import ru.urururu.sanity.api.cfg.Primitive;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.util.FinalMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class TypeParser<M, T> implements ParserListener<M> {
    protected final Map<T, Type> typesCache = FinalMap.createHashMap();
    private final Map<T, Type> structCache = FinalMap.createHashMap();

    public abstract Type parse(T type);

    private Type get(T type) {
        return structCache.getOrDefault(type, null);
    }

    private void cache(T type, Type struct) {
        structCache.put(type, struct);
    }

    protected static Type createInt(long width) {
        return new Primitive("int" + width) {
            @Override
            public boolean isInteger() {
                return true;
            }
        };
    }

    private static Primitive createFloat(String name) {
        return new Primitive(name) {
            @Override
            public boolean isFloatingPoint() {
                return true;
            }
        };
    }

    protected static Type createFloat() {
        return createFloat("float");
    }

    protected static Primitive createDouble() {
        return createFloat("double");
    }

    protected static Primitive createLongDouble() {
        return createFloat("long double");
    }

    protected static Type createMetadata() {
        return createPrimitive("metadata");
    }

    private static Primitive createPrimitive(String name) {
        return new Primitive(name);
    }

    protected static Primitive createVoid() {
        return new Primitive("void") {
            @Override
            public boolean isVoid() {
                return true;
            }
        };
    }

    protected PointerType createPointer(T baseType) {
        return new PointerType(parse(baseType));
    }

    protected ArrayType createArray(T elementType, long length) {
        return new ArrayType(parse(elementType), length);
    }

    protected Type createStruct(T originalType, String name, Iterable<T> originalFieldTypes) {
        Type cached = get(originalType);
        if (cached != null) {
            return cached;
        }

        final List<Type> fieldTypes = new ArrayList<>();

        Type struct = new StructType(fieldTypes, name);

        cache(originalType, struct);

        StreamSupport.stream(originalFieldTypes.spliterator(), false).map(this::parse).forEach(fieldTypes::add);

        return struct;
    }

    protected Type createFunction(T originalReturnType, Iterable<T> originalParamTypes) {
        final Type returnType = parse(originalReturnType);
        final Type[] paramsType = StreamSupport.stream(originalParamTypes.spliterator(), false).map(this::parse).toArray(Type[]::new);

        return new FunctionType(returnType, paramsType);
    }

    @Override
    public void onModuleFinished(M module) {
        structCache.clear();
        typesCache.clear();
    }

    private static class StructType implements Type {
        private final List<Type> fieldTypes;
        private final String name;

        StructType(List<Type> fieldTypes, String name) {
            this.fieldTypes = fieldTypes;
            this.name = name;
        }

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
    }

    private static class FunctionType implements Type {
        private final Type returnType;
        private final Type[] paramsType;

        FunctionType(Type returnType, Type[] paramsType) {
            this.returnType = returnType;
            this.paramsType = paramsType;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

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
    }
}
