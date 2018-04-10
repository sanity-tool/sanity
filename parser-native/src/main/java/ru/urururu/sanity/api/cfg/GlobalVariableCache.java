package ru.urururu.sanity.api.cfg;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.cpp.ParserListener;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class GlobalVariableCache implements ParserListener {
    Map<Pair<String, Type>, GlobalVar> cache = new HashMap<>();
    int count;

    public RValue get(String name, Type type) {
        if (name.isEmpty()) {
            name = "global" + count++;
        }
        return cache.computeIfAbsent(new ImmutablePair<>(name, type),p -> new GlobalVar(p.getKey(), p.getValue()));
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module) {
        count = 0;
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
    }
}
