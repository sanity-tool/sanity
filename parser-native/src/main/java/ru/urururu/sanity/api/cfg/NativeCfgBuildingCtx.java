package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.cpp.NativeParsersFacade;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.Iterables;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class NativeCfgBuildingCtx extends CfgBuildingCtx<SWIGTYPE_p_LLVMOpaqueModule, SWIGTYPE_p_LLVMOpaqueType,
        SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {

    public NativeCfgBuildingCtx(NativeParsersFacade parsers, SWIGTYPE_p_LLVMOpaqueValue function) {
        super(parsers);

        SWIGTYPE_p_LLVMOpaqueValue param = bitreader.LLVMGetFirstParam(function);
        while (param != null) {
            params.put(param, new Parameter(params.size(), bitreader.LLVMGetValueName(param), parsers.parse(bitreader.LLVMTypeOf(param))));
            param = bitreader.LLVMGetNextParam(param);
        }
    }

    public LValue getOrCreateTmpVar(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        return getOrCreateTmpVar(instruction, bitreader.LLVMTypeOf(instruction));
    }

    public Cfe getLabel(SWIGTYPE_p_LLVMOpaqueValue label) {
        SWIGTYPE_p_LLVMOpaqueBasicBlock block = bitreader.LLVMValueAsBasicBlock(label);

        Cfe result = getBlockEntrance(block);

        SWIGTYPE_p_LLVMOpaqueValue instruction = bitreader.LLVMGetFirstInstruction(block);
        if (bitreader.LLVMIsAPHINode(instruction) != null) {
            int n = Math.toIntExact(bitreader.LLVMCountIncoming(instruction));
            return prependPhiAssignment(instruction, result,
                    Iterables.indexed(i -> bitreader.LLVMGetIncomingBlock(instruction, i), n),
                    Iterables.indexed(i -> bitreader.LLVMGetIncomingValue(instruction, i), n));
        }

        return result;
    }

    @Override
    protected String blockToString(SWIGTYPE_p_LLVMOpaqueBasicBlock block) {
        return bitreader.LLVMPrintValueToString(bitreader.LLVMBasicBlockAsValue(block));
    }
}
