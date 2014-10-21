package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
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
