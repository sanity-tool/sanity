package na.okutane.cpp;

import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.api.cfg.ConstCache;
import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.RValue;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMValueRef;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;

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

    public RValue parseLValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMValueRef value) {
        if (bitreader.LLVMIsAGlobalVariable(value) != null) {
            return globals.get(bitreader.LLVMGetValueName(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAArgument(value) != null) {
            return ctx.getParam(value);
        }
        throw new IllegalStateException("Can't parse LValue: " + bitreader.LLVMPrintValueToString(value));
    }

    public RValue parseRValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMValueRef value) {
        if (bitreader.LLVMIsAInstruction(value) != null) {
            return instructionParser.parseValue(ctx, value);
        }
        if (bitreader.LLVMIsAConstantExpr(value) != null) {
            return instructionParser.parseConst(ctx, value);
        }
        if (bitreader.LLVMIsAConstantInt(value) != null) {
            return constants.get(bitreader.LLVMConstIntGetSExtValue(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAConstantFP(value) != null) {
            return constants.get(bitreader.GetConstantFPDoubleValue(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        if (bitreader.LLVMIsAConstantPointerNull(value) != null) {
            return constants.getNull(typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        check(value, bitreader::LLVMIsAConstantStruct, "bitreader::LLVMIsAConstantStruct");
        check(value, bitreader::LLVMIsAConstantAggregateZero, "bitreader::LLVMIsAConstantAggregateZero");
        check(value, bitreader::LLVMIsAConstantArray, "bitreader::LLVMIsAConstantArray");
        check(value, bitreader::LLVMIsAConstantDataArray, "bitreader::LLVMIsAConstantDataArray");
        check(value, bitreader::LLVMIsAConstantDataSequential, "bitreader::LLVMIsAConstantDataSequential");
        check(value, bitreader::LLVMIsAConstantDataVector, "bitreader::LLVMIsAConstantDataVector");
        if (bitreader.LLVMIsAFunction(value) != null) {
            return constants.getFunction(bitreader.LLVMGetValueName(value), typeParser.parse(bitreader.LLVMTypeOf(value)));
        }
        return parseLValue(ctx, value);
    }

    private void check(SWIGTYPE_p_LLVMValueRef value, Function<SWIGTYPE_p_LLVMValueRef, SWIGTYPE_p_LLVMValueRef> test, String err) {
        if (test.apply(value) != null) {
            throw new IllegalStateException(err);
        }
    }
}
