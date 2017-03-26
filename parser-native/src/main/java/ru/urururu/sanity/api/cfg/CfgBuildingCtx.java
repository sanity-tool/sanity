package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.cpp.SourceRangeFactory;
import ru.urururu.sanity.cpp.TypeParser;
import ru.urururu.sanity.cpp.ValueParser;
import ru.urururu.sanity.cpp.llvm.LLVMOpcode;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CfgBuildingCtx {
    private final TypeParser typeParser;
    private final ValueParser valueParser;
    private final SourceRangeFactory sourceRangeFactory;

    Map<SWIGTYPE_p_LLVMOpaqueValue, RValue> params = new HashMap<>();
    Map<SWIGTYPE_p_LLVMOpaqueValue, LValue> tmpVars = new HashMap<>();
    Map<SWIGTYPE_p_LLVMOpaqueBasicBlock, Cfe> labels = new HashMap<>();
    private SWIGTYPE_p_LLVMOpaqueBasicBlock block;

    public CfgBuildingCtx(TypeParser typeParser, ValueParser valueParser, SourceRangeFactory sourceRangeFactory, SWIGTYPE_p_LLVMOpaqueValue function) {
        this.typeParser = typeParser;
        this.valueParser = valueParser;
        this.sourceRangeFactory = sourceRangeFactory;

        SWIGTYPE_p_LLVMOpaqueValue param = bitreader.LLVMGetFirstParam(function);
        while (param != null) {
            params.put(param, new Parameter(params.size(), bitreader.LLVMGetValueName(param), typeParser.parse(bitreader.LLVMTypeOf(param))));
            param = bitreader.LLVMGetNextParam(param);
        }
    }

    public LValue getOrCreateTmpVar(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            result = new TemporaryVar(typeParser.parse(bitreader.LLVMTypeOf(instruction)));
            tmpVars.put(instruction, result);
        }
        return result;
    }

    public LValue getTmpVar(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            throw new IllegalStateException("not created yet");
        }
        return result;
    }

    public RValue getParam(SWIGTYPE_p_LLVMOpaqueValue value) {
        return params.get(value);
    }

    public Cfe getLabel(SWIGTYPE_p_LLVMOpaqueValue label) {
        SWIGTYPE_p_LLVMOpaqueBasicBlock block = bitreader.LLVMValueAsBasicBlock(label);

        Cfe result = labels.get(block);
        if (result == null) {
            result = new NoOp(null);
            labels.put(block, result);
        }

        SWIGTYPE_p_LLVMOpaqueValue instruction = bitreader.LLVMGetFirstInstruction(block);
        if (bitreader.LLVMIsAPHINode(instruction) != null) {
            long n = bitreader.LLVMCountIncoming(instruction);
            for (int i = 0; i < n; i++) {
                if (this.block.equals(bitreader.LLVMGetIncomingBlock(instruction, i))) {
                    Assignment phiAssignment = new Assignment(
                            getOrCreateTmpVar(instruction),
                            valueParser.parseRValue(this, bitreader.LLVMGetIncomingValue(instruction, i)),
                            sourceRangeFactory.getSourceRange(instruction)
                    );
                    phiAssignment.setNext(result);

                    return phiAssignment;
                }
            }
        }

        return result;
    }

    public CfgBuildingCtx enterSubCfg(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock) {
        this.block = entryBlock;
        return this;
    }
}
