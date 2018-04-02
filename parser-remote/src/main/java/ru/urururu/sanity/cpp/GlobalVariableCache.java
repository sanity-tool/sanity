package ru.urururu.sanity.cpp;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.GlobalVar;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.Type;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class GlobalVariableCache {
    Map<Pair<String, Type>, GlobalVar> cache = new HashMap<>();
    int count;

    public RValue get(String name, Type type) {
        if (name.isEmpty()) {
            name = "global" + count++;
        }
        return cache.computeIfAbsent(new ImmutablePair<>(name, type), p -> new GlobalVar(p.getKey(), p.getValue()));
    }
}
