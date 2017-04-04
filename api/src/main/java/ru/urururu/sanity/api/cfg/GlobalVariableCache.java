package ru.urururu.sanity.api.cfg;

import javafx.util.Pair;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.ParserListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class GlobalVariableCache<M> implements ParserListener<M> {
    private Map<Pair<String, Type>, GlobalVar> cache = new HashMap<>();
    private int count;

    public RValue get(String name, Type type) {
        if (name.isEmpty()) {
            name = "global" + count++;
        }
        return cache.computeIfAbsent(new Pair<>(name, type), p -> new GlobalVar(p.getKey(), p.getValue()));
    }

    @Override
    public void onModuleStarted(M module) {
        count = 0;
    }
}
