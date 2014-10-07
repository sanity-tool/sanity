package na.okutane.api.cfg;

import na.okutane.api.Cfg;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class CfePrinter implements CfeVisitor {
    final StringBuilder sb = new StringBuilder();
    final Map<TemporaryVar, Integer> tmpVars = new HashMap<TemporaryVar, Integer>();

    private CfePrinter() {

    }

    @Override
    public String toString() {
        return sb.toString();
    }

    private boolean isEmpty() {
        return sb.length() == 0;
    }

    public void printLine() {
        sb.append(System.getProperty("line.separator"));
    }

    private CfePrinter print0(Cfe cfe) {
        cfe.accept(this);

        SourceRange sourceRange = cfe.getSourceRange();
        if (sourceRange == null) {
            sb.append("\n - no source -");
        } else {
            sb.append('\n').append(sourceRange);
        }

        return this;
    }

    public static String print(Cfe cfe) {
        return new CfePrinter().print0(cfe).toString();
    }

    public static String print(Cfg cfg) {
        CfePrinter printer = new CfePrinter();
        Cfe cfe = cfg.getEntry();

        while (cfe != null) {
            if (!printer.isEmpty()) {
                printer.printLine();
            }
            printer.print0(cfe);
            cfe = cfe.getNext();
        }

        return printer.toString();
    }

    @Override
    public void visit(UnprocessedElement element) {
        sb.append("noop: ").append(element.getMessage());
    }

    @Override
    public void visit(Assignment assignment) {
        sb.append("assign: ");
        print(assignment.getLeft());
        sb.append(" = ");
        print(assignment.getRight());
    }

    private void print(RValue value) {
        if (value instanceof TemporaryVar) {
            Integer number = tmpVars.get(value);
            if (number == null) {
                number = tmpVars.size();
                tmpVars.put((TemporaryVar) value, number);
            }
            sb.append("tmp").append(number);
            return;
        }
        if (value instanceof BinaryExpression) {
            BinaryExpression expression = (BinaryExpression) value;
            print(expression.getLeft());
            sb.append(' ').append(expression.getOperator()).append(' ');
            print(expression.getRight());
            return;
        }
        if (value instanceof ConstCache.Const) {
            ConstCache.Const constant = (ConstCache.Const) value;
            sb.append(constant.getValue());
            return;
        }
        if (value instanceof ConstCache.NullPtr) {
            sb.append("null");
            return;
        }
        sb.append(value.toString());
    }
}
