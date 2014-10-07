package na.okutane.api.cfg;

import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class Call extends Cfe {
    private final String name;
    private final LValue lValue;
    private final List<RValue> args;

    public Call(String name, LValue lValue, List<RValue> args, SourceRange sourceRange) {
        super(sourceRange);
        this.name = name;
        this.lValue = lValue;
        this.args = args;
    }

    public String getName() {
        return name;
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
