package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.LValue;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.TemporaryVar;
import ru.urururu.util.FinalMap;

import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class CfgBuildingCtx<T, V, I, B, Ctx/*todo?*/ extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    protected final ParsersFacade<T, V, I, B, Ctx> parsers;

    protected Map<V, RValue> params = FinalMap.createHashMap();
    Map<I, LValue> tmpVars = FinalMap.createHashMap();
    protected Map<B, Cfe> labels = FinalMap.createHashMap();
    protected B block;

    protected CfgBuildingCtx(ParsersFacade<T, V, I, B, Ctx> parsers) {
        this.parsers = parsers;
    }

    protected LValue getOrCreateTmpVar(I instruction, T type) {
        return tmpVars.computeIfAbsent(instruction, k -> new TemporaryVar(parsers.parse(type)));
    }

    public abstract LValue getOrCreateTmpVar(I instruction);

    public abstract Cfe getLabel(V label);

    public LValue getTmpVar(I instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            throw new IllegalStateException("not created yet");
        }
        return result;
    }

    public CfgBuildingCtx<T, V, I, B, Ctx> enterSubCfg(CfgBuildingCtx<T, V, I, B, Ctx> ctx, B entryBlock) {
        this.block = entryBlock;
        return this;
    }
}
