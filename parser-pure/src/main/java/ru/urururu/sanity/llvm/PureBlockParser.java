package ru.urururu.sanity.llvm;

import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.BlockParser;
import ru.urururu.sanity.api.cfg.Cfe;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureBlockParser extends BlockParser<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(PureCfgBuildingCtx ctx, Block block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx, block.getInstructions());
    }
}
