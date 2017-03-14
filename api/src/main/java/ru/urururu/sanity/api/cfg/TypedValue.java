package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class TypedValue implements RValue {
    protected final Type type;

    public TypedValue(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
