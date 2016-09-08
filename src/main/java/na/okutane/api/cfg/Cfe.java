package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public abstract class Cfe {
    Cfe next;
    private final SourceRange sourceRange;

    public Cfe(SourceRange sourceRange) {
        this.sourceRange = sourceRange;
    }

    public SourceRange getSourceRange() {
        return sourceRange;
    }

    public Cfe getNext() {
        return next;
    }

    public void setNext(Cfe next) {
        this.next = next;
    }

    public abstract void accept(CfeVisitor visitor);

    @Override
    public String toString() {
        return CfePrinter.print(this);
    }
}