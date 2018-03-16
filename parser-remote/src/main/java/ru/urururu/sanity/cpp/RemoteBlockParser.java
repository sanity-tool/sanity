package ru.urururu.sanity.cpp;

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
public class RemoteBlockParser extends BlockParser<SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, RemoteCfgBuildingCtx> {
    @Override
    protected Cfe processBlock(RemoteCfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock block) {
        ctx.beginSubCfg(block);
        return processBlock(ctx,
                Iterables.linked(() -> bitreader.LLVMGetFirstInstruction(block), bitreader::LLVMGetNextInstruction));
    }
}
