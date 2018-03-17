package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.ValueRefDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BlockParser;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.NativeCfgBuildingCtx;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;
import ru.urururu.util.Iterables;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteBlockParser extends BlockParser<Integer,
        ValueRefDto, ValueRefDto, BlockDto, RemoteCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(RemoteCfgBuildingCtx ctx, BlockDto block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx,
                Iterables.linked(() -> bitreader.LLVMGetFirstInstruction(block), bitreader::LLVMGetNextInstruction));
    }
}
