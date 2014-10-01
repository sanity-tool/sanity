package na.okutane.cpp;

import na.okutane.api.cfg.LValue;
import na.okutane.api.cfg.RValue;
import na.okutane.cpp.llvm.Value;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class ValueParser {
    public LValue parseLValue(Value value) {
        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    public RValue parseRValue(Value value) {
        throw new IllegalStateException("Can't parse RValue: " + value);
    }
}
