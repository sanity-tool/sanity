package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Type {
    Type getElementType();

    Type getFieldType(int index);

    String getFieldName(int index);

    default Type getReturnType() {
        throw new IllegalStateException("not a function, but a " + getClass().getSimpleName());
    }

    default boolean isVoid() {
        return false;
    }
}
