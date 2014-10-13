package na.okutane.api.cfg;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class GlobalVariableCache {
    public RValue get(String name, Type type) {
        return new GlobalVar(name, type);
    }

    public static class GlobalVar extends TypedValue {
        private final String name;

        public GlobalVar(String name, Type type) {
            super(type);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
