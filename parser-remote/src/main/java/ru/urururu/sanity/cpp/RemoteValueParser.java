package ru.urururu.sanity.cpp;

import io.swagger.client.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.RValue;

import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteValueParser extends ValueParser<Integer,
        ValueRefDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> implements ParserListener {
    @Autowired
    GlobalVariableCache globals;
    @Autowired
    ConstCache constants;
    @Autowired
    RemoteParsersFacade parsers;

    private ModuleDto currentModule;

    public RValue parseLValue(RemoteCfgBuildingCtx ctx, ValueRefDto value) {
        if (value.getKind() == ValueRefDto.KindEnum.GLOBAL) {
            ValueDto valueDto = currentModule.getGlobals().get(value.getIndex());
            if (valueDto.getKind().equals("LLVMGlobalVariableValueKind")) {
                return globals.get(valueDto.getName(), parsers.parse(valueDto.getTypeId()));
            }

            throw new IllegalStateException("Can't parse LValue: " + valueDto);
        }
        if (value.getKind() == ValueRefDto.KindEnum.ARGUMENT) {
            return ctx.getParam(value);
        }

        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    public RValue parseRValue(RemoteCfgBuildingCtx ctx, ValueRefDto value) {
        if (value.getKind() == ValueRefDto.KindEnum.INSTRUCTION) {
            return parsers.parseInstructionValue(ctx, ctx.function.getBlocks().get(value.getBlockIndex()).getInstructions().get(value.getIndex()));
        }
        if (value.getKind() == ValueRefDto.KindEnum.GLOBAL) {
            ValueDto valueDto = currentModule.getGlobals().get(value.getIndex());
            if (valueDto.getKind().equals("LLVMConstantExprValueKind")) {
                InstructionDto fakeInstruction = new InstructionDto();
                fakeInstruction.setKind(valueDto.getOpcode());
                fakeInstruction.setOperands(valueDto.getOperands());
                fakeInstruction.setTypeId(valueDto.getTypeId());

                return parsers.parseInstructionConst(ctx, fakeInstruction);
            }
            if (valueDto.getKind().equals("LLVMFunctionValueKind")) {
                return constants.getFunction(valueDto.getName(), parsers.parse(valueDto.getTypeId()));
            }
            if (valueDto.getKind().equals("LLVMConstantIntValueKind")) {
                return constants.get(valueDto.getIntValue(), parsers.parse(valueDto.getTypeId()));
            }
            if (valueDto.getKind().equals("LLVMConstantFPValueKind")) {
                return constants.get(valueDto.getFpValue(), parsers.parse(valueDto.getTypeId()));
            }
            if (valueDto.getKind().equals("LLVMConstantPointerNullValueKind")) {
                return constants.getNull(parsers.parse(valueDto.getTypeId()));
            }
        }

        return parseLValue(ctx, value);
    }

    private void check(ValueRefDto value, Function<ValueRefDto, ValueRefDto> test, String err) {
        if (test.apply(value) != null) {
            throw new IllegalStateException(err);
        }
    }


    @Override
    public void onModuleStarted(ModuleDto module) {
        currentModule = module;
    }

    @Override
    public void onModuleFinished(ModuleDto module) {
        currentModule = null;
    }
}
