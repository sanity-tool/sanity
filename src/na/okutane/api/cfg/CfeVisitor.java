package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public interface CfeVisitor {
    void visit(UnprocessedElement element);

    void visit(Assignment assignment);

    void visit(Call call);
}
