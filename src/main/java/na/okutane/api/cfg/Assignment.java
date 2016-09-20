package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Assignment extends Cfe {
    private final LValue left;
    private final RValue right;

    public Assignment(LValue left, RValue right, SourceRange sourceRange) {
        super(sourceRange);
        this.left = left;
        this.right = right;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }

    public LValue getLeft() {
        return left;
    }

    public RValue getRight() {
        return right;
    }
}
