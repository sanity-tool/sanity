package na.okutane.api.cfg;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class ConstCache {
    public Const get(long value, Type type) {
        return new Const(value, type);
    }

    public RValue getNull(Type type) {
        return new NullPtr(type);
    }

    public static class NullPtr extends TypedValue {
        public NullPtr(Type type) {
            super(type);
        }
    }

    public static class Const extends TypedValue {
        private final long value;

        public Const(long value, Type type) {
            super(type);
            this.value = value;
        }

        public long getValue() {
            return value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }
    }
}
