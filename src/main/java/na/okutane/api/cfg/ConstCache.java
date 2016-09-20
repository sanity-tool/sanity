package na.okutane.api.cfg;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ConstCache {
    public Const get(long value, Type type) {
        return new Const(value, type);
    }

    public RValue get(double value, Type type) {
        return new RealConst(value, type);
    }

    public RValue get(String s, Type type) {
        return new StringConst(s, type);
    }

    public RValue getNull(Type type) {
        return new NullPtr(type);
    }

    public RValue getFunction(String name, Type type) {
        return new FunctionAddress(name, type);
    }

    public static class NullPtr extends TypedValue implements Value {
        public NullPtr(Type type) {
            super(type);
        }
    }

    public static class Const extends TypedValue implements Value {
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

    public static class RealConst extends TypedValue implements Value {
        private final double value;

        public RealConst(double value, Type type) {
            super(type);
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

    public static class StringConst extends TypedValue implements Value {
        private final String value;

        public StringConst(String value, Type type) {
            super(type);
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return '"' + value + '"';
        }
    }

    public static class FunctionAddress extends TypedValue implements Value {
        private final String name;

        public FunctionAddress(String name, Type type) {
            super(type);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return '@' + name;
        }
    }
}
