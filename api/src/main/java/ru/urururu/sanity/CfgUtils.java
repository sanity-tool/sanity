package ru.urururu.sanity;

import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.*;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class CfgUtils {
    public static Set<Cfe> getAllCfes(Cfe entry) {
        Set<Cfe> cfes = new LinkedHashSet<>();
        collect(cfes, entry);
        return cfes;
    }

    private static void collect(Set<Cfe> cfes, Cfe cfe) {
        // todo rewrite it :)
        if (cfe == null) {
            return;
        }
        if (cfes.add(cfe)) {
            if (cfe instanceof IfCondition) {
                collect(cfes, ((IfCondition) cfe).getThenElement());
                collect(cfes, ((IfCondition) cfe).getElseElement());
            } else if (cfe instanceof Switch) {
                collect(cfes, ((Switch) cfe).getDefaultCase());
                for (Cfe caseLabel : ((Switch) cfe).getCases().values()) {
                    collect(cfes, caseLabel);
                }
            } else if (cfe.getNext() != null) {
                collect(cfes, cfe.getNext());
            }
        }
    }

    public Cfe removeNoOps(Cfe entry) {
        boolean modified;
        do {
            modified = false;
            Set<Cfe> cfes = getAllCfes(entry);

            Map<Cfe, Integer> usages = new HashMap<>();
            for (Cfe cfe : cfes) {
                if (cfe instanceof IfCondition) {
                    addUsage(usages, ((IfCondition) cfe).getThenElement());
                    addUsage(usages, ((IfCondition) cfe).getElseElement());
                } else if (cfe instanceof Switch) {
                    addUsage(usages, ((Switch) cfe).getDefaultCase());
                    for (Cfe caseLabel : ((Switch) cfe).getCases().values()) {
                        addUsage(usages, caseLabel);
                    }
                } else {
                    addUsage(usages, cfe.getNext());
                }
            }

            for (Cfe cfe : cfes) {
                if (cfe instanceof IfCondition) {
                    IfCondition ifCondition = (IfCondition) cfe;
                    Cfe thenElement = ifCondition.getThenElement();
                    if (isRemovable(usages, cfe, thenElement)) {
                        ifCondition.setThenElement(thenElement.getNext());
                        modified = true;
                    }
                    Cfe elseElement = ifCondition.getElseElement();
                    if (isRemovable(usages, cfe, elseElement)) {
                        ifCondition.setElseElement(elseElement.getNext());
                        modified = true;
                    }
                } else if (cfe instanceof Switch) {
                    Switch switchElement = (Switch) cfe;
                    Cfe defaultElement = switchElement.getDefaultCase();
                    if (isRemovable(usages, cfe, defaultElement)) {
                        switchElement.setDefaultCase(defaultElement.getNext());
                        modified = true;
                    }
                    for (Map.Entry<RValue, Cfe> caseDef : switchElement.getCases().entrySet()) {
                        Cfe caseLabel = caseDef.getValue();
                        if (isRemovable(usages, cfe, caseLabel)) {
                            caseDef.setValue(caseLabel.getNext());
                        }
                    }
                } else {
                    if (isRemovable(usages, cfe, cfe.getNext())) {
                        cfe.setNext(cfe.getNext().getNext());
                        modified = true;
                    }
                }
            }
        } while (modified);

        return entry;


    }

    private boolean isRemovable(Map<Cfe, Integer> usages, Cfe current, Cfe next) {
        return current != next && isSingleUsedNoOp(usages, next);
    }

    private boolean isSingleUsedNoOp(Map<Cfe, Integer> usages, Cfe cfe) {
        // todo think about source range comparison. if different - it's better to have tmp var assignment to preserve source reference.
        return usages.getOrDefault(cfe, 1) == 1 && cfe instanceof NoOp;
    }

    private void addUsage(Map<Cfe, Integer> usages, Cfe cfe) {
        usages.compute(cfe, (unused, count) -> count == null ? 1 : count + 1);
    }
}
