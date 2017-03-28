package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class FunctionAddress extends TypedValue implements Value {
    private final String name;

    public FunctionAddress(String name, Type type) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return '@' + name;
    }
}
