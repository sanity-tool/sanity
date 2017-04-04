package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.SourceRange;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongParsersFacade implements ParsersFacade<Type, Symbol, InstructionBlock> {
    @Autowired
    SulongValueParser valueParser;

    @Autowired
    SulongTypeParser typeParser;

    @Autowired
    SulongInstructionParser instructionParser;

    @Autowired
    SulongSourceRangeFactory sourceRangeFactory;

    @Override
    public ru.urururu.sanity.api.cfg.Type parse(Type type) {
        return typeParser.parse(type);
    }

    @Override
    public RValue parseLValue(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, Symbol value) {
        return valueParser.parseLValue(ctx, value);
    }

    @Override
    public RValue parseRValue(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, Symbol value) {
        return valueParser.parseRValue(ctx, value);
    }

    @Override
    public SourceRange getSourceRange(Symbol instruction) {
        return sourceRangeFactory.getSourceRange(instruction);
    }

    @Override
    public Cfe parse(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, Symbol instruction) {
        return instructionParser.parse(ctx, instruction);
    }
}
