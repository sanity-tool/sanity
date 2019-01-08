package ru.urururu.sanity.api.cfg;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class LocalVar extends TypedValue {
    private String name;
    private SourceRange allocationRange;

    public LocalVar(String name, Type type) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return StringUtils.defaultIfEmpty(name, getDefaultName());
    }

    public void setName(String name) {
        if (StringUtils.isEmpty(name)) {
            return; // ignoring, we probably already have better name (older clang)
        }

        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public SourceRange getAllocationRange() {
        return allocationRange;
    }

    public void setAllocationRange(SourceRange sourceRange) {
        this.allocationRange = sourceRange;
    }

    protected String getDefaultName() {
        return "<unnamed>";
    }
}
