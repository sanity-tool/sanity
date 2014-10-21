package na.okutane.api.cfg;

import na.okutane.CfgUtils;
import na.okutane.api.Cfg;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class CfePrinter implements CfeVisitor {
    final StringBuilder sb = new StringBuilder();
    final Map<RValue, Integer> tmpVars = new HashMap<RValue, Integer>();
    final Map<Cfe, Integer> cfeIds = new HashMap<Cfe, Integer>();

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
        sb.append(getId(cfe)).append(' ');

        cfe.accept(this);

        if (!(cfe instanceof IfCondition) && cfe.getNext() == null) {
            sb.append(" <exit>");
        }

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

        Set<Cfe> cfes = CfgUtils.getAllCfes(cfg.getEntry());

        for (Cfe cfe : cfes) {
            if (!printer.isEmpty()) {
                printer.printLine();
            }
            printer.print0(cfe);
        }

        return printer.toString();
    }

    @Override
    public void visit(UnprocessedElement element) {
        sb.append("noop: ").append(element.getMessage());
    }

    @Override
    public void visit(NoOp noOp) {
        sb.append("noop");
    }

    @Override
    public void visit(Assignment assignment) {
        sb.append("assign: ");
        print(assignment.getLeft());
        sb.append(" = ");
        print(assignment.getRight());
    }

    @Override
    public void visit(Call call) {
        sb.append("call: ");
        LValue lvalue = call.getlValue();
        if (lvalue != null) {
            print(lvalue);
            sb.append(" = ");
        }
        sb.append(call.getName()).append('(');

        List<RValue> args = call.getArgs();
        if (!args.isEmpty()) {
            Iterator<RValue> it = args.iterator();
            print(it.next());
            while (it.hasNext()) {
                sb.append(", ");
                print(it.next());
            }
        }

        sb.append(')');
    }

    @Override
    public void visit(IfCondition ifCondition) {
        sb.append("if: ");
        print(ifCondition.getCondition());
        sb.append(" then ").append(getId(ifCondition.getThenElement())).append(" else ").append(getId(ifCondition.getElseElement()));
    }

    private void print(RValue value) {
        if (value instanceof TemporaryVar) {
            Integer number = getOrCreateId(value, tmpVars);
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
        if (value instanceof Indirection) {
            sb.append('*');
            print(((Indirection) value).getPointer());
            return;
        }
        sb.append(value.toString());
    }

    private String getId(Cfe cfe) {
        return String.format("#%04d:\t", getOrCreateId(cfe, cfeIds));
    }

    private <K> Integer getOrCreateId(K value, Map<K, Integer> map) {
        Integer number = map.get(value);
        if (number == null) {
            number = map.size();
            map.put(value, number);
        }
        return number;
    }
}
