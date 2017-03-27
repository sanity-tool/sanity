package ru.urururu.sanity.api.cfg;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Parameter extends TypedValue {
    private final int index;
    private final String name;

    public Parameter(int index, String name, Type type) {
        super(type);
        this.index = index;
        this.name = StringUtils.defaultIfBlank(name, "<param " + index + ">");
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
