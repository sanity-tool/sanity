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
public abstract class TypeParser<T> {
    protected final Map<T, Type> typesCache = FinalMap.createHashMap();
    protected final Map<T, Type> structCache = FinalMap.createHashMap();

    public abstract Type parse(T type);

    private Type get(T type) {
        return structCache.get(type);
    }

    private void cache(T type, Type struct) {
        structCache.put(type, struct);
    }

    protected static Type createInt(long width) {
        return new Primitive("int" + width);
    }

    protected static Type createFloat() {
        return createPrimitive("float");
    }

    protected static Primitive createDouble() {
        return createPrimitive("double");
    }

    protected static Primitive createLongDouble() {
        return createPrimitive("long double");
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

        cache(originalType, struct);

        StreamSupport.stream(originalFieldTypes.spliterator(), false).map(this::parse).forEach(fieldTypes::add);

        return struct;
    }

    protected Type createFunction(T originalReturnType, Iterable<T> originalParamTypes) {
        final Type returnType = parse(originalReturnType);
        final Type[] paramsType = StreamSupport.stream(originalParamTypes.spliterator(), false).map(this::parse).toArray(Type[]::new);

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
