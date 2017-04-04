package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionParameter;
import com.oracle.truffle.llvm.parser.model.globals.GlobalVariable;
import com.oracle.truffle.llvm.parser.model.symbols.constants.Constant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.NullConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.StringConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.FloatConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.IntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.runtime.types.FunctionType;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.RValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongValueParser extends ValueParser<Symbol, Type, InstructionBlock> {
    @Override
    public RValue parseLValue(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, Symbol value) {
        if (value instanceof GlobalVariable) {
            return globals.get(fixName(((GlobalVariable) value).getName()), typeParser.parse(value.getType()));
        }
        if (value instanceof FunctionParameter) {
            return ctx.getParam(value);
        }
        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    @Override
    public RValue parseRValue(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, Symbol value) {
        if (value == null) {
            throw new IllegalArgumentException("value: " + value);
        }

        if (value instanceof Instruction) {
            return instructionParser.parseValue(ctx, value);
        }
        if (value instanceof NullConstant) {
            ru.urururu.sanity.api.cfg.Type constantType = typeParser.parse(value.getType());
            if (constantType.isInteger()) {
                return constants.get(0, constantType);
            }
            if (constantType.isFloatingPoint()) {
                return constants.get(0.0, constantType);
            }
            if (constantType.isPointer()) {
                return constants.getNull(constantType);
            }
            // todo there probably should be zero-initialization, but for now we do nothing.
            return null;
        }
        if (value instanceof IntegerConstant) {
            return constants.get(((IntegerConstant) value).getValue(), typeParser.parse(value.getType()));
        }
        if (value instanceof FloatConstant) {
            return constants.get(((FloatConstant) value).getValue(), typeParser.parse(value.getType()));
        }
        if (value instanceof StringConstant) {
            return constants.get(((StringConstant) value).getString(), typeParser.parse(value.getType()));
        }
        if (value instanceof FunctionType) {
            return constants.getFunction(fixName(((FunctionType) value).getName()), typeParser.parse(value.getType()));
        }
        if (value instanceof Constant) {
            return instructionParser.parseConst(ctx, value);
        }

        return parseLValue(ctx, value);
    }

    private String fixName(String name) {
        return name.substring(1);
    }
}
