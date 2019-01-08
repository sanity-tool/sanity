package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.ValueParser;

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

    @Autowired
    ValueParser<T, V, I, B, Ctx> valueParser;

    @Autowired
    BlockParser<T, V, I, B, Ctx> blockParser;

    @Autowired
    private ParserSettings settings;

    public RValue parseRValue(Ctx ctx, V value) {
        return valueParser.parseRValue(ctx, value);
    }

    public Type parse(T type) {
        return typeParser.parse(type);
    }

    public Cfe parseBlock(Ctx ctx, B block) {
        return blockParser.processBlock(ctx, block);
    }

    public Cfe parse(Ctx ctx, I instruction) {
        return instructionParser.parse(ctx, instruction);
    }

    public RValue parseInstructionValue(Ctx ctx, I instruction) {
        return instructionParser.parseValue(ctx, instruction);
    }

    public RValue parseInstructionConst(Ctx ctx, I instruction) {
        return instructionParser.parseConst(ctx, instruction);
    }

    public SourceRange getSourceRange(I instruction) {
        return sourceRangeFactory.getSourceRange(instruction);
    }

    public ParserSettings getSettings() {
        return settings;
    }
}
