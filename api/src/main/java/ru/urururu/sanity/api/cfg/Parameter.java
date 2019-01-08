package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Parameter extends LocalVar {
    private final int index;

    public Parameter(int index, String name, Type type) {
        super(name, type);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    protected String getDefaultName() {
        return "<param " + index + ">";
    }
}
