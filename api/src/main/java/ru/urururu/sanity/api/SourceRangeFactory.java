package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.SourceRange;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class SourceRangeFactory<I> {
    public abstract SourceRange getSourceRange(I instruction);
}
