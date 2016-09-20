package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class IfCondition extends Cfe {
    private final RValue condition;
    private Cfe thenElement;
    private Cfe elseElement;

    public IfCondition(RValue condition, Cfe thenElement, Cfe elseElement, SourceRange sourceRange) {
        super(sourceRange);
        this.condition = condition;
        this.thenElement = thenElement;
        this.elseElement = elseElement;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }

    public RValue getCondition() {
        return condition;
    }

    public Cfe getThenElement() {
        return thenElement;
    }

    public Cfe getElseElement() {
        return elseElement;
    }

    @Override
    public void setNext(Cfe next) {
        throw new IllegalStateException("can't set next");
    }

    public void setThenElement(Cfe thenElement) {
        this.thenElement = thenElement;
    }

    public void setElseElement(Cfe elseElement) {
        this.elseElement = elseElement;
    }
}
