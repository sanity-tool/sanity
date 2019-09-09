package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.*;
import ru.urururu.util.FinalMap;

import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class CfgBuildingCtx<T, V, I, B, Ctx/*todo?*/ extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    protected final ParsersFacade<T, V, I, B, Ctx> parsers;

    protected Map<V, RValue> params = FinalMap.createHashMap();
    private Map<I, LocalVar> localVars = FinalMap.createHashMap();
    private Map<I, LValue> tmpVars = FinalMap.createHashMap();
    protected Map<B, Cfe> labels = FinalMap.createHashMap();
    public B block;
    protected CfgBuilder builder;

    protected CfgBuildingCtx(ParsersFacade<T, V, I, B, Ctx> parsers) {
        this.parsers = parsers;
    }

    protected LocalVar getOrCreateLocalVar(I instruction, String name, T type) {
        return localVars.computeIfAbsent(instruction, k -> new LocalVar(name, parsers.parse(type)));
    }

    public abstract LocalVar getOrCreateLocalVar(I instruction);

    protected LValue getOrCreateTmpVar(I instruction, T type) {
        return tmpVars.computeIfAbsent(instruction, k -> new TemporaryVar(parsers.parse(type)));
    }

    public abstract LValue getOrCreateTmpVar(I instruction);

    public abstract Cfe getLabel(V label);

    public Cfe endSubCfg() {
        try {
            return builder.getResult();
        } finally {
            builder = null;
        }
    }

    public void append(Cfe cfe) {
        builder.append(cfe);
    }

    public LValue getTmpVar(I instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            throw new IllegalStateException("not created yet");
        }
        return result;
    }

    public void beginSubCfg(B entryBlock) {
        block = entryBlock;
        builder = new CfgBuilder();
    }
}
