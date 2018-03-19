package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.InstructionDto;
import io.swagger.client.model.ValueRefDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BlockParser;
import ru.urururu.sanity.api.cfg.Cfe;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteBlockParser extends BlockParser<Integer,
        ValueRefDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(RemoteCfgBuildingCtx ctx, BlockDto block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx, block.getInstructions());
    }
}
