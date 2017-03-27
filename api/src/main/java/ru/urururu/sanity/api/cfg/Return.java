package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Return extends Cfe {
    private final RValue value;

    public Return(RValue value, SourceRange sourceRange) {
        super(sourceRange);
        this.value = value;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }

    public RValue getValue() {
        return value;
    }
}
