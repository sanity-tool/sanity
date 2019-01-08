package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Allocation extends Cfe {
    private final LocalVar local;

    public Allocation(LocalVar local, SourceRange sourceRange) {
        super(sourceRange);
        this.local = local;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }

    public LocalVar getLocal() {
        return local;
    }

    @Override
    public SourceRange getSourceRange() {
        if (local.getAllocationRange() != null) {
            return local.getAllocationRange();
        }

        return super.getSourceRange();
    }
}
