package na.okutane.api.cfg;

import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class GlobalVariableCache {
    public LValue get(String name) {
        return new GlobalVar(name);
    }

    public static class GlobalVar implements LValue {
        private final String name;

        public GlobalVar(String name) {

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
