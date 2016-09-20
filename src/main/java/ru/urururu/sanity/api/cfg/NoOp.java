package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class NoOp extends Cfe {
    public NoOp(SourceRange sourceRange) {
        super(sourceRange);
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }
}
