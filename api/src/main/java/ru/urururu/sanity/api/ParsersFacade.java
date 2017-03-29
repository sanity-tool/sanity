package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.sanity.api.cfg.Type;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class ParsersFacade<T, V, I, B, Ctx extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    @Autowired
    InstructionParser<T, V, I, B, Ctx> instructionParser;
    @Autowired
    TypeParser<T> typeParser;
    @Autowired
    SourceRangeFactory<I> sourceRangeFactory;

    public Type parse(T type) {
        return typeParser.parse(type);
    }

    public abstract RValue parseRValue(Ctx ctx, V value);

    public Cfe parse(Ctx ctx, I instruction) {
        return instructionParser.parse(ctx, instruction);
    }

    public SourceRange getSourceRange(I instruction) {
        return sourceRangeFactory.getSourceRange(instruction);
    }
}
