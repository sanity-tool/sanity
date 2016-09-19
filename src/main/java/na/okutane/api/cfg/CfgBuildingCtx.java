package na.okutane.api.cfg;

import na.okutane.cpp.TypeParser;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CfgBuildingCtx {
    private final TypeParser typeParser;
    Map<SWIGTYPE_p_LLVMOpaqueValue, RValue> params = new HashMap<>();
    Map<SWIGTYPE_p_LLVMOpaqueValue, LValue> tmpVars = new HashMap<>();
    Map<SWIGTYPE_p_LLVMOpaqueBasicBlock, Cfe> labels = new HashMap<>();

    public CfgBuildingCtx(TypeParser typeParser, SWIGTYPE_p_LLVMOpaqueValue function) {
        this.typeParser = typeParser;
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
        return result;
    }
}
