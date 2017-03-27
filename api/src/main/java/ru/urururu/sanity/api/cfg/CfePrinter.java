package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.Cfg;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CfePrinter implements CfeVisitor {
    final StringBuilder sb = new StringBuilder();
    final Map<RValue, Integer> tmpVars = new HashMap<>();
    final Map<Cfe, Integer> cfeIds = new HashMap<>();
    final Set<Cfe> printed = new HashSet<>();

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
        printed.add(cfe);

        cfe.accept(this);

        if (!(cfe instanceof IfCondition || cfe instanceof Switch)) {
            if (cfe.getNext() == null) {
                sb.append(" <exit>");
            }
            if (printed.contains(cfe.getNext())) {
                sb.append(" next: ").append(getId(cfe.getNext()).trim());
            }
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
        Set<Cfe> cfes = CfgUtils.getAllCfes(cfg.getEntry());

        return printAll(cfes);
    }

    public static String printAll(Collection<Cfe> cfes) {
        CfePrinter printer = new CfePrinter();

        for (Cfe cfe : cfes) {
            if (!printer.isEmpty()) {
                printer.printLine();
            }
            printer.print0(cfe);
        }

        return printer.toString();
    }

    public static String printValue(RValue value) {
        CfePrinter printer = new CfePrinter();

        printer.print(value);

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
    public void visit(Return returnStatement) {
        sb.append("return ");
        print(returnStatement.getValue());
    }

    @Override
    public void visit(Call call) {
        sb.append("call: ");
        LValue lvalue = call.getlValue();
        if (lvalue != null) {
            print(lvalue);
            sb.append(" = ");
        }
        RValue function = call.getFunction();
        if (function instanceof ConstCache.FunctionAddress) {
            sb.append(((ConstCache.FunctionAddress) function).getName());
        } else {
            print(function);
        }
        sb.append('(');

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
        sb.append(" then ").append(getId(ifCondition.getThenElement())).append(" else ").append(getId(ifCondition.getElseElement()).trim());
    }

    @Override
    public void visit(Switch switchElement) {
        sb.append("switch: ");
        print(switchElement.getControl());
        sb.append(" default ").append(getId(switchElement.getDefaultCase()));
        for (Map.Entry<RValue, Cfe> entry : switchElement.getCases().entrySet()) {
            sb.append(' ');
            print(entry.getKey());
            sb.append(" -> ").append(getId(entry.getValue()));
        }
        sb.setLength(sb.length() - 1); // todo get rid of trailing whitespace in getId
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
        if (value instanceof GetElementPointer) {
            RValue index = ((GetElementPointer) value).getIndex();
            if (index instanceof ConstCache.Const && ((ConstCache.Const) index).getValue() == 0) {
                print(((GetElementPointer) value).getPointer());
            } else {
                sb.append('(');
                print(((GetElementPointer) value).getPointer());
                sb.append('+');
                print(index);
                sb.append(')');
            }
            return;
        }
        if (value instanceof GetFieldPointer) {
            sb.append('(');
            print(((GetFieldPointer) value).getPointer());
            sb.append('.');
            sb.append(((GetFieldPointer) value).getPointer().getType().getFieldName(((GetFieldPointer) value).getIndex()));
            sb.append(')');
            return;
        }
        sb.append(value.toString());
    }

    private String getId(Cfe cfe) {
        if (cfe == null) {
            return "<exit>";
        }
        return String.format("#%04d:\t", getOrCreateId(cfe, cfeIds)); // todo remove trailing whitespace
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
