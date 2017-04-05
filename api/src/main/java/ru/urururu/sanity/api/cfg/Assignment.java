package ru.urururu.sanity.api.cfg;

import scala.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Assignment extends Cfe {
    private final LValue left;
    private final RValue right;

    public Assignment(LValue left, RValue right, SourceRange sourceRange) {
        super(sourceRange);

        if (left == null) {
            throw new IllegalArgumentException("left: " + left);
        }
        if (right == null) {
            throw new IllegalArgumentException("right: " + right);
        }

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

    public static Option<Tuple2<LValue, RValue>> unapply(Cfe cfe) {
        if (cfe instanceof Assignment) {
            Assignment assign = (Assignment) cfe;
            return Option.apply(Tuple2.apply(assign.getLeft(), assign.getRight()));
        }
        
        return Option.empty();
    }
}
