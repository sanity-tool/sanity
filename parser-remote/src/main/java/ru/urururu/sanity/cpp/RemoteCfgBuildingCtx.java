package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.InstructionDto;
import io.swagger.client.model.TypeDto;
import io.swagger.client.model.ValueDto;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.NativeParsersFacade;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RemoteCfgBuildingCtx extends CfgBuildingCtx<TypeDto,
        ValueDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> {

    public RemoteCfgBuildingCtx(RemoteParsersFacade parsers, ValueDto function) {
        super(parsers);

        ValueDto param = bitreader.LLVMGetFirstParam(function);
        while (param != null) {
            params.put(param, new Parameter(params.size(), bitreader.LLVMGetValueName(param), parsers.parse(bitreader.LLVMTypeOf(param))));
            param = bitreader.LLVMGetNextParam(param);
        }
    }

    public LValue getOrCreateTmpVar(InstructionDto instruction) {
        return getOrCreateTmpVar(instruction, bitreader.LLVMTypeOf(instruction));
    }

    public RValue getParam(ValueDto value) {
        return params.get(value);
    }

    public Cfe getLabel(ValueDto label) {
        BlockDto block = bitreader.LLVMValueAsBasicBlock(label);

        Cfe result = labels.computeIfAbsent(block, k -> new NoOp(null));

        for (InstructionDto instruction : block.getInstructions()) {

        }

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
        }

        return result;
    }
}
