package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.cpp.NativeParsersFacade;
import ru.urururu.sanity.cpp.llvm.*;

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
            long n = bitreader.LLVMCountIncoming(instruction);
            for (int i = 0; i < n; i++) {
                if (this.block.equals(bitreader.LLVMGetIncomingBlock(instruction, i))) {
                    Assignment phiAssignment = new Assignment(
                            getOrCreateTmpVar(instruction),
                            parsers.parseRValue(this, bitreader.LLVMGetIncomingValue(instruction, i)),
                            parsers.getSourceRange(instruction)
                    );
                    phiAssignment.setNext(result);

                    return phiAssignment;
                }
            }

            throw new IllegalStateException("Can't match incoming block: " + bitreader.LLVMPrintValueToString(bitreader.LLVMBasicBlockAsValue(this.block)) + " to one of ");
        }

        return result;
    }
}
