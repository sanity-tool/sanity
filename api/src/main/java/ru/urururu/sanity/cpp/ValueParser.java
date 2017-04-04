package ru.urururu.sanity.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.ParsersFacade;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.GlobalVariableCache;
import ru.urururu.sanity.api.cfg.RValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class ValueParser<M, T, V, I, B, Ctx extends CfgBuildingCtx<M, T, V, I, B, Ctx>> {
    @Autowired
    protected ParsersFacade<M, T, V, I, B, Ctx> parsers;
    @Autowired
    protected
    GlobalVariableCache globals;
    @Autowired
    protected
    ConstCache constants;

    public abstract RValue parseLValue(Ctx ctx, V value);

    public abstract RValue parseRValue(Ctx ctx, V value);
}
