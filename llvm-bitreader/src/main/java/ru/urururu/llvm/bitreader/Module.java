package ru.urururu.llvm.bitreader;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Module {
    private final List<Function> functionsWithBodies;
    private final List<GlobalVariable> globalVariables;
    private final Map<String, Object> namedMetadata;

    public Module(List<Function> functionsWithBodies, List<GlobalVariable> globalVariables, Map<String, Object> namedMetadata) {
        this.functionsWithBodies = functionsWithBodies;
        this.globalVariables = globalVariables;
        this.namedMetadata = namedMetadata;
    }

    public IntegerValue getModuleFlag(String flagName) {
        List<Object> flags = (List<Object>) namedMetadata.get("llvm.module.flags");
        if (flags == null) {
            return null;
        }

        for (Object flag : flags) {
            List<Object> flagDetails = (List<Object>) flag;
            if (flagDetails.size() == 3 && flagDetails.get(1).equals(flagName)) {
                return (IntegerValue) flagDetails.get(2);
            }
        }

        return null;
    }

    public List<Function> getFunctionsWithBodies() {
        return functionsWithBodies;
    }

    public List<GlobalVariable> getGlobalVariables() {
        return globalVariables;
    }
}
