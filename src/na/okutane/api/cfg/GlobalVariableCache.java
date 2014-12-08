package na.okutane.api.cfg;

import na.okutane.cpp.ParserListener;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class GlobalVariableCache implements ParserListener {
    int count;

    public RValue get(String name, Type type) {
        if (name.isEmpty()) {
            name = "global" + count++;
        }
        return new GlobalVar(name, type);
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module) {
        count = 0;
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
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
