package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.ValueParser;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class ParsersFacade<M, T, V, I, B, Ctx extends CfgBuildingCtx<M, T, V, I, B, Ctx>> {
    @Autowired
    InstructionParser<M, T, V, I, B, Ctx> instructionParser;
    @Autowired
    TypeParser<M, T> typeParser;
    @Autowired
    SourceRangeFactory<I> sourceRangeFactory;
    @Autowired
    ValueParser<M, T, V, I, B, Ctx> valueParser;
    @Autowired
    BlockParser<M, T, V, I, B, Ctx> blockParser;

    public RValue parseLValue(Ctx ctx, V value) {
        return valueParser.parseLValue(ctx, value);
    }

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

    public RValue parseInstructionConst(Ctx ctx, V instruction) {
        return instructionParser.parseConst(ctx, instruction);
    }

    public SourceRange getSourceRange(I instruction) {
        return sourceRangeFactory.getSourceRange(instruction);
    }
}
