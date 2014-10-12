package na.okutane.api.cfg;

import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class CfgBuildingCtx {
    Map<SWIGTYPE_p_LLVMOpaqueValue, LValue> params = new HashMap<SWIGTYPE_p_LLVMOpaqueValue, LValue>();
    Map<SWIGTYPE_p_LLVMOpaqueValue, LValue> tmpVars = new HashMap<SWIGTYPE_p_LLVMOpaqueValue, LValue>();

    public CfgBuildingCtx(SWIGTYPE_p_LLVMOpaqueValue function) {
        SWIGTYPE_p_LLVMOpaqueValue param = bitreader.LLVMGetFirstParam(function);
        while (param != null) {
            params.put(param, new Parameter(params.size(), bitreader.LLVMGetValueName(param)));
            param = bitreader.LLVMGetNextParam(param);
        }
    }

    public LValue getTmpVar(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            result = new TemporaryVar();
            tmpVars.put(instruction, result);
        }
        return result;
    }

    public LValue getParam(SWIGTYPE_p_LLVMOpaqueValue value) {
        return params.get(value);
    }
}
