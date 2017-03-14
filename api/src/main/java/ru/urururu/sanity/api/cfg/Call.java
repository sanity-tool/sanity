package ru.urururu.sanity.api.cfg;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Call extends Cfe {
    private final RValue function;
    private final LValue lValue;
    private final List<RValue> args;

    public Call(RValue function, LValue lValue, List<RValue> args, SourceRange sourceRange) {
        super(sourceRange);
        this.function = function;
        this.lValue = lValue;
        this.args = args;
    }

    public RValue getFunction() {
        return function;
    }

    public LValue getlValue() {
        return lValue;
    }

    public List<RValue> getArgs() {
        return args;
    }

    @Override
    public void accept(CfeVisitor visitor) {
        visitor.visit(this);
    }
}
