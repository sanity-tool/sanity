package na.okutane.api.cfg;

import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class CfgBuildingCtx {
    Map<SWIGTYPE_p_LLVMOpaqueValue, LValue> tmpVars = new HashMap<SWIGTYPE_p_LLVMOpaqueValue, LValue>();

    public LValue getTmpVar(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            result = new TemporaryVar();
            tmpVars.put(instruction, result);
        }
        return result;
    }
}
