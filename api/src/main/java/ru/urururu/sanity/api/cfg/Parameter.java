package ru.urururu.sanity.api.cfg;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Parameter extends LocalVar {
    private final int index;

    public Parameter(int index, String name, Type type) {
        super(StringUtils.defaultIfBlank(name, "<param " + index + ">"), type);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
