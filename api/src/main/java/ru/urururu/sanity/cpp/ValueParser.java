package ru.urururu.sanity.cpp;

import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.RValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class ValueParser<T, V, I, B, Ctx extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    public abstract RValue parseRValue(Ctx ctx, V value);
}
