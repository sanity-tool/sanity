package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.TypeDto;
import io.swagger.client.model.ValueDto;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.ParsersFacade;
import ru.urururu.sanity.api.cfg.NativeCfgBuildingCtx;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteParsersFacade extends ParsersFacade<TypeDto,
        ValueDto, ValueDto, BlockDto, RemoteCfgBuildingCtx> {
}
