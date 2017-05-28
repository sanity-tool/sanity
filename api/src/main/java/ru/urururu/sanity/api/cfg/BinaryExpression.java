package ru.urururu.sanity.api.cfg;

import scala.Option;
import scala.Tuple3;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
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

    public static Option<Tuple3<RValue, Operator, RValue>> unapply(RValue rValue) {
        if (rValue instanceof BinaryExpression) {
            BinaryExpression expression = (BinaryExpression) rValue;
            return Option.apply(Tuple3.apply(expression.getLeft(), expression.getOperator(), expression.getRight()));
        }

        return Option.empty();
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
        ShiftRight(">>"),
        Gt(">"),
        Ge(">="),
        Lt("<"),
        Le("<="),
        Eq("=="),
        Ne("!=")
        ;

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
