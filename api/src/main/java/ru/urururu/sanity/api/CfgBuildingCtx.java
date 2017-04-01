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
    private Map<I, LValue> tmpVars = FinalMap.createHashMap();
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

    public Cfe endSubCfg() {
        throw new IllegalStateException("not subCfg ctx");
    }

    public void append(Cfe cfe) {
        throw new IllegalStateException("not subCfg ctx");
    }

    public LValue getTmpVar(I instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            throw new IllegalStateException("not created yet");
        }
        return result;
    }

    public CfgBuildingCtx<T, V, I, B, Ctx> beginSubCfg(B entryBlock) {
        CfgBuildingCtx<T, V, I, B, Ctx> parent = this;
        this.block = entryBlock;
        return new CfgBuildingCtx<T, V, I, B, Ctx>(parsers) {
            CfgBuilder builder = new CfgBuilder();

            @Override
            public LValue getOrCreateTmpVar(I instruction) {
                return parent.getOrCreateTmpVar(instruction);
            }

            @Override
            public Cfe getLabel(V label) {
                return parent.getLabel(label);
            }

            @Override
            public void append(Cfe cfe) {
                builder.append(cfe);
            }

            @Override
            public Cfe endSubCfg() {
                return builder.getResult();
            }
        };
    }
}
