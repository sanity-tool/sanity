package na.okutane.api.cfg;

import javafx.util.Pair;
import na.okutane.cpp.ParserListener;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMModuleRef;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class GlobalVariableCache implements ParserListener {
    Map<Pair<String, Type>, GlobalVar> cache = new HashMap<>();
    int count;

    public RValue get(String name, Type type) {
        if (name.isEmpty()) {
            name = "global" + count++;
        }
        return cache.computeIfAbsent(new Pair<>(name, type), p -> new GlobalVar(p.getKey(), p.getValue()));
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMModuleRef module) {
        count = 0;
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMModuleRef module) {
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
