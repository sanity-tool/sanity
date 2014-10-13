package na.okutane.cpp;

import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.api.cfg.ConstCache;
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
    ConstCache constants;
    @Autowired
    InstructionParser instructionParser;
    @Autowired
    TypeParser typeParser;

    public RValue parseLValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAGlobalVariable(value) != null) {
            return globals.get(bitreader.LLVMGetValueName(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAArgument(value) != null) {
            return ctx.getParam(value);
        }
        if (bitreader.LLVMIsAFunction(value) != null) {
            throw new IllegalStateException("functions not supported yet");
        }
        throw new IllegalStateException("Can't parse LValue: " + bitreader.LLVMPrintValueToString(value));
    }

    public RValue parseRValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue value) {
        if (bitreader.LLVMIsAInstruction(value) != null) {
            return instructionParser.parseValue(ctx, value);
        }
        if (bitreader.LLVMIsAConstantInt(value) != null) {
            return constants.get(bitreader.LLVMConstIntGetSExtValue(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAConstantPointerNull(value) != null) {
            return constants.getNull(typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        return parseLValue(ctx, value);
    }
}
