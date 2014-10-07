package na.okutane.api.cfg;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class ConstCache {
    public Const get(long value) {
        return new Const(value);
    }

    public RValue getNull() {
        return new NullPtr();
    }

    public static class NullPtr implements RValue {

    }

    public static class Const implements RValue {
        private final long value;

        public Const(long value) {
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }
}
