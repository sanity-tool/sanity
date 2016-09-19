package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class UnprocessedElement extends Cfe {
    private final String message;

    public UnprocessedElement(String message, SourceRange sourceRange) {
        super(sourceRange);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }
}
