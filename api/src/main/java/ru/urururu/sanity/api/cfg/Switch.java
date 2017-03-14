package ru.urururu.sanity.api.cfg;

import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Switch extends Cfe {
    private final RValue control;
    private Cfe defaultCase;
    private final Map<RValue, Cfe> cases;

    public Switch(RValue control, Cfe defaultCase, Map<RValue, Cfe> cases, SourceRange sourceRange) {
        super(sourceRange);
        this.control = control;
        this.defaultCase = defaultCase;
        this.cases = cases;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }

    public RValue getControl() {
        return control;
    }

    public Cfe getDefaultCase() {
        return defaultCase;
    }

    public void setDefaultCase(Cfe defaultCase) {
        this.defaultCase = defaultCase;
    }

    public Map<RValue, Cfe> getCases() {
        return cases;
    }

    @Override
    public void setNext(Cfe next) {
        throw new IllegalStateException("can't set next");
    }
}
