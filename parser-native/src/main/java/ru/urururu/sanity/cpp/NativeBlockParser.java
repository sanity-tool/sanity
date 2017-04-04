package ru.urururu.sanity.cpp;

import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BlockParser;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.NativeCfgBuildingCtx;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.Iterables;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBlockParser extends BlockParser<SWIGTYPE_p_LLVMOpaqueModule, SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(NativeCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx,
                Iterables.linked(bitreader.LLVMGetFirstInstruction(block), bitreader::LLVMGetNextInstruction));
    }
}
