package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class StringConst extends TypedValue implements Value {
    private final String value;

    public StringConst(String value, Type type) {
        super(type);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return '"' + value + '"';
    }
}
