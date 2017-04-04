package ru.urururu.sanity.llvm;

import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.cpp.ValueParser;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureValueParser extends ValueParser<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
    @Override
    public RValue parseLValue(PureCfgBuildingCtx ctx, Value value) {
        if (value instanceof GlobalVariable) {
            return globals.get(value.getName(), parsers.parse(value.getType()));
        }
        if (value instanceof Argument) {
            return ctx.getParam(value);
        }
        throw new IllegalStateException("Can't parse LValue: " + value.toString());
    }

    @Override
    public RValue parseRValue(PureCfgBuildingCtx ctx, Value value) {
        if (value instanceof Instruction) {
            return parsers.parseInstructionValue(ctx, (Instruction) value);
        }
//        if (bitreader.LLVMIsAConstantExpr(value) != null) {
//            return parsers.parseInstructionConst(ctx, value);
//        }
        if (value instanceof IntegerValue) {
            return constants.get(((IntegerValue) value).getValue(), parsers.parse(value.getType()));
        }
//        if (bitreader.LLVMIsAConstantFP(value) != null) {
//            return constants.get(bitreader.GetConstantFPDoubleValue(value), parsers.parse(bitreader.LLVMTypeOf(value)));
//        }
        if (value instanceof NullValue) {
            return constants.getNull(parsers.parse(value.getType()));
        }
//        check(value, bitreader::LLVMIsAConstantStruct, "bitreader::LLVMIsAConstantStruct");
//        check(value, bitreader::LLVMIsAConstantAggregateZero, "bitreader::LLVMIsAConstantAggregateZero");
//        check(value, bitreader::LLVMIsAConstantArray, "bitreader::LLVMIsAConstantArray");
//        check(value, bitreader::LLVMIsAConstantDataArray, "bitreader::LLVMIsAConstantDataArray");
//        check(value, bitreader::LLVMIsAConstantDataSequential, "bitreader::LLVMIsAConstantDataSequential");
//        check(value, bitreader::LLVMIsAConstantDataVector, "bitreader::LLVMIsAConstantDataVector");
        if (value instanceof Function) {
            return constants.getFunction(value.getName(), parsers.parse(value.getType()));
        }
        return parseLValue(ctx, value);
    }
}
