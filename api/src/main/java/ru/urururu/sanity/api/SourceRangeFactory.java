package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.util.Coverage;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class SourceRangeFactory<I> {
    public abstract SourceRange getSourceRange(I instruction);

    protected SourceRange getSourceRange(String filename, int line) {
        if (!new File(filename).exists()) {
            return null;
        }

        Coverage.markAsCode(filename, line);
        return new SourceRange(filename, line);
    }
}
