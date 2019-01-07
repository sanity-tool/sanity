package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class LocalVar extends TypedValue {
    private final String name;
    private SourceRange allocationRange;

    public LocalVar(String name, Type type) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public SourceRange getAllocationRange() {
        return allocationRange;
    }

    public void setAllocationRange(SourceRange sourceRange) {
        this.allocationRange = sourceRange;
    }
}
