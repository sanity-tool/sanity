package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class BinaryExpression implements RValue {
    private final Operator operator;
    private final RValue left;
    private final RValue right;

    public BinaryExpression(RValue left, Operator operator, RValue right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public RValue getLeft() {
        return left;
    }

    public RValue getRight() {
        return right;
    }

    @Override
    public Type getType() {
        throw new IllegalStateException();
    }

    public enum Operator {
        Add("+"),
        Sub("-"),
        Mul("*"),
        Div("/"),
        Rem("%"),
        And("&"),
        Or("|"),
        Xor("^"),
        ShiftLeft("<<"),
        ShiftRight(">>");

        private final String stringValue;

        Operator(String stringValue) {
            this.stringValue = stringValue;
        }

        @Override
        public String toString() {
            return stringValue;
        }
    }
}
