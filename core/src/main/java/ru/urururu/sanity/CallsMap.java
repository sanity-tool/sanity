package ru.urururu.sanity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;

import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Scope("prototype")
@Component
public class CallsMap implements CfeVisitor {
    Map<String, List<Cfe>> staticCalls = new LinkedHashMap<>();
    Set<String> tookPointers = new LinkedHashSet<>();
    Map<Type, List<Cfe>> compatibleCalls = new LinkedHashMap<>();

    public void init(List<Cfg> cfgs) {
        for (Cfg cfg : cfgs) {
            staticCalls.put(cfg.getId(), new ArrayList<>());
        }

        for (Cfg cfg : cfgs) {
            for (Cfe cfe : CfgUtils.getAllCfes(cfg.getEntry())) {
                cfe.accept(this);
            }
        }

        for (Iterator<Map.Entry<String, List<Cfe>>> it = staticCalls.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, List<Cfe>> entry = it.next();
            if (entry.getValue().isEmpty()) {
                it.remove();
            }
        }
    }

    public Map<String, List<Cfe>> getStaticCalls() {
        return staticCalls;
    }

    public Set<String> getTookPointers() {
        return tookPointers;
    }

    public Map<Type, List<Cfe>> getCompatibleCalls() {
        return compatibleCalls;
    }

    @Override
    public void visit(UnprocessedElement element) {

    }

    @Override
    public void visit(Assignment assignment) {
        if (assignment.getRight() instanceof ConstCache.FunctionAddress) {
            tookPointers.add(((ConstCache.FunctionAddress) assignment.getRight()).getName());
        }
    }

    @Override
    public void visit(Call call) {
        if (call.getFunction() instanceof ConstCache.FunctionAddress) {
            List<Cfe> cfes = staticCalls.get(((ConstCache.FunctionAddress) call.getFunction()).getName());
            if (cfes != null) {
                cfes.add(call);
            }
        } else {
            List<Cfe> cfes = compatibleCalls.get(call.getFunction().getType());
            if (cfes == null) {
                cfes = new ArrayList<>();
                compatibleCalls.put(call.getFunction().getType(), cfes);
            }
            cfes.add(call);
        }
    }

    @Override
    public void visit(IfCondition ifCondition) {

    }

    @Override
    public void visit(Switch switchElement) {

    }

    @Override
    public void visit(NoOp noOp) {

    }
}
