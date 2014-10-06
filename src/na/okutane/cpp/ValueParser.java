package na.okutane.cpp;

import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.LValue;
import na.okutane.api.cfg.RValue;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class ValueParser {
    @Autowired
    GlobalVariableCache globals;
    @Autowired
    InstructionParser instructionParser;

    public LValue parseLValue(SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAGlobalVariable(value) != null) {
            return globals.get(bitreader.LLVMGetValueName(value));
        }

        throw new IllegalStateException("Can't parse LValue: " + bitreader.LLVMPrintValueToString(value));
    }

    public RValue parseRValue(SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAInstruction(value) != null) {
            return instructionParser.parseValue(value);
        }
        throw new IllegalStateException("Can't parse RValue: " + bitreader.LLVMPrintValueToString(value));
    }
}
