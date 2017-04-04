package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BlockParser;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.util.Iterables;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongBlockParser extends BlockParser<ModelModule, com.oracle.truffle.llvm.runtime.types.Type, Symbol,
        Instruction, InstructionBlock, SuCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(SuCfgBuildingCtx ctx, InstructionBlock block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx, Iterables.indexed(block::getInstruction, block.getInstructionCount()));
    }
}
