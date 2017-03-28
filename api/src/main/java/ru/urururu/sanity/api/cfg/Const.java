package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Const extends TypedValue implements Value {
    private final long value;

    public Const(long value, Type type) {
        super(type);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
