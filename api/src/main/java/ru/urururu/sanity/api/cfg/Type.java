package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Type {
    Type getElementType();

    Type getFieldType(int index);

    default String getFieldName(int index) {
        throw new IllegalStateException("not a structure, but a " + getClass().getSimpleName());
    }

    default Type getReturnType() {
        throw new IllegalStateException("not a function, but a " + getClass().getSimpleName());
    }

    default boolean isVoid() {
        return false;
    }

    default boolean isInteger() {
        return false;
    }

    default boolean isFloatingPoint() {
        return false;
    }

    default boolean isPointer() {
        return false;
    }
}
