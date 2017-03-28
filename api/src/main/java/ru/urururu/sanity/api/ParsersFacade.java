package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.sanity.api.cfg.Type;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class ParsersFacade<T, V, I, B, Ctx> {
    public abstract Type parse(T type);

    public abstract RValue parseRValue(Ctx ctx, V value);

    public abstract SourceRange getSourceRange(I instruction);
}
