package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionParameter;
import com.oracle.truffle.llvm.parser.model.globals.GlobalValueSymbol;
import com.oracle.truffle.llvm.parser.model.globals.GlobalVariable;
import com.oracle.truffle.llvm.parser.model.symbols.constants.Constant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.NullConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.StringConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.FloatConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.IntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.runtime.types.FunctionType;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.RValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongValueParser extends ValueParser<ModelModule, com.oracle.truffle.llvm.runtime.types.Type, Symbol,
        Instruction, InstructionBlock, SuCfgBuildingCtx> {
    @Override
    public RValue parseLValue(SuCfgBuildingCtx ctx, Symbol value) {
        if (value instanceof GlobalValueSymbol) {
            return globals.get(fixName(((GlobalValueSymbol) value).getName()), parsers.parse(value.getType()));
        }
        if (value instanceof FunctionParameter) {
            return ctx.getParam(value);
        }
        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    @Override
    public RValue parseRValue(SuCfgBuildingCtx ctx, Symbol value) {
        if (value == null) {
            throw new IllegalArgumentException("value: " + value);
        }

        if (value instanceof Instruction) {
            return parsers.parseInstructionValue(ctx, (Instruction) value);
        }
        if (value instanceof NullConstant) {
            ru.urururu.sanity.api.cfg.Type constantType = parsers.parse(value.getType());
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
            return constants.get(((IntegerConstant) value).getValue(), parsers.parse(value.getType()));
        }
        if (value instanceof FloatConstant) {
            return constants.get(((FloatConstant) value).getValue(), parsers.parse(value.getType()));
        }
        if (value instanceof StringConstant) {
            return constants.get(((StringConstant) value).getString(), parsers.parse(value.getType()));
        }
        if (value instanceof FunctionType) {
            return constants.getFunction(fixName(((FunctionType) value).getName()), parsers.parse(value.getType()));
        }
        if (value instanceof Constant) {
            return parsers.parseInstructionConst(ctx, value);
        }

        return parseLValue(ctx, value);
    }

    private String fixName(String name) {
        return name.substring(1);
    }
}
