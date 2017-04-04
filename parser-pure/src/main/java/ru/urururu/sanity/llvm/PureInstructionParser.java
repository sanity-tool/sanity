package ru.urururu.sanity.llvm;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.InstructionParser;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.RValue;

import java.util.Arrays;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureInstructionParser extends InstructionParser<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
    @Override
    protected Cfe doParse(PureCfgBuildingCtx ctx, Instruction instruction) {
        switch (instruction.getCode()) {
            case FUNC_CODE_INST_BINOP:
                break;
            case FUNC_CODE_INST_CAST:
                return null;
            case FUNC_CODE_INST_GEP_OLD:
                break;
            case FUNC_CODE_INST_SELECT:
                break;
            case FUNC_CODE_INST_EXTRACTELT:
                break;
            case FUNC_CODE_INST_INSERTELT:
                break;
            case FUNC_CODE_INST_SHUFFLEVEC:
                break;
            case FUNC_CODE_INST_CMP:
                break;
            case FUNC_CODE_INST_RET:
                return createReturn(ctx, instruction);
            case FUNC_CODE_INST_BR:
                break;
            case FUNC_CODE_INST_SWITCH:
                break;
            case FUNC_CODE_INST_INVOKE:
                break;
            case FUNC_CODE_INST_UNREACHABLE:
                break;
            case FUNC_CODE_INST_PHI:
                break;
            case FUNC_CODE_INST_ALLOCA:
            case FUNC_CODE_INST_LOAD:
                return null;
            case FUNC_CODE_INST_VAARG:
                break;
            case FUNC_CODE_INST_STORE_OLD:
                break;
            case FUNC_CODE_INST_EXTRACTVAL:
                break;
            case FUNC_CODE_INST_INSERTVAL:
                break;
            case FUNC_CODE_INST_CMP2:
                break;
            case FUNC_CODE_INST_VSELECT:
                break;
            case FUNC_CODE_INST_INBOUNDS_GEP_OLD:
                break;
            case FUNC_CODE_INST_INDIRECTBR:
                break;
            case FUNC_CODE_INST_CALL:
                return createCall(ctx, instruction, instruction.getOperands()[instruction.getOperands().length - 1], Arrays.asList(instruction.getOperands()).subList(0, instruction.getOperands().length - 1));
            case FUNC_CODE_INST_FENCE:
                break;
            case FUNC_CODE_INST_CMPXCHG_OLD:
                break;
            case FUNC_CODE_INST_ATOMICRMW:
                break;
            case FUNC_CODE_INST_RESUME:
                break;
            case FUNC_CODE_INST_LANDINGPAD_OLD:
                break;
            case FUNC_CODE_INST_LOADATOMIC:
                break;
            case FUNC_CODE_INST_STOREATOMIC_OLD:
                break;
            case FUNC_CODE_INST_GEP:
                break;
            case FUNC_CODE_INST_STORE:
                return createStore(ctx, instruction, instruction.getOperands()[0], instruction.getOperands()[1]);
            case FUNC_CODE_INST_STOREATOMIC:
                break;
            case FUNC_CODE_INST_CMPXCHG:
                break;
            case FUNC_CODE_INST_LANDINGPAD:
                break;
            case FUNC_CODE_INST_CLEANUPRET:
                break;
            case FUNC_CODE_INST_CATCHRET:
                break;
            case FUNC_CODE_INST_CATCHPAD:
                break;
            case FUNC_CODE_INST_CLEANUPPAD:
                break;
            case FUNC_CODE_INST_CATCHSWITCH:
                break;
            case FUNC_CODE_OPERAND_BUNDLE:
                break;
        }

        throw new NotImplementedException(instruction.getCode().name());
    }

    @Override
    public RValue parseValue(PureCfgBuildingCtx ctx, Instruction value) {
        switch (value.getCode()) {
            case FUNC_CODE_DECLAREBLOCKS:
                break;
            case FUNC_CODE_INST_BINOP:
                break;
            case FUNC_CODE_INST_CAST:
                return parsers.parseRValue(ctx, value.getOperands()[0]);
            case FUNC_CODE_INST_GEP_OLD:
                break;
            case FUNC_CODE_INST_SELECT:
                break;
            case FUNC_CODE_INST_EXTRACTELT:
                break;
            case FUNC_CODE_INST_INSERTELT:
                break;
            case FUNC_CODE_INST_SHUFFLEVEC:
                break;
            case FUNC_CODE_INST_CMP:
                break;
            case FUNC_CODE_INST_RET:
                break;
            case FUNC_CODE_INST_BR:
                break;
            case FUNC_CODE_INST_SWITCH:
                break;
            case FUNC_CODE_INST_INVOKE:
                break;
            case FUNC_CODE_INST_UNREACHABLE:
                break;
            case FUNC_CODE_INST_PHI:
                break;
            case FUNC_CODE_INST_ALLOCA:
                return ctx.getOrCreateTmpVar(value);
            case FUNC_CODE_INST_LOAD:
                return createLoad(ctx, value.getOperands()[0]);
            case FUNC_CODE_INST_VAARG:
                break;
            case FUNC_CODE_INST_STORE_OLD:
                break;
            case FUNC_CODE_INST_EXTRACTVAL:
                break;
            case FUNC_CODE_INST_INSERTVAL:
                break;
            case FUNC_CODE_INST_CMP2:
                break;
            case FUNC_CODE_INST_VSELECT:
                break;
            case FUNC_CODE_INST_INBOUNDS_GEP_OLD:
                break;
            case FUNC_CODE_INST_INDIRECTBR:
                break;
            case FUNC_CODE_DEBUG_LOC_AGAIN:
                break;
            case FUNC_CODE_INST_CALL:
                break;
            case FUNC_CODE_DEBUG_LOC:
                break;
            case FUNC_CODE_INST_FENCE:
                break;
            case FUNC_CODE_INST_CMPXCHG_OLD:
                break;
            case FUNC_CODE_INST_ATOMICRMW:
                break;
            case FUNC_CODE_INST_RESUME:
                break;
            case FUNC_CODE_INST_LANDINGPAD_OLD:
                break;
            case FUNC_CODE_INST_LOADATOMIC:
                break;
            case FUNC_CODE_INST_STOREATOMIC_OLD:
                break;
            case FUNC_CODE_INST_GEP:
                break;
            case FUNC_CODE_INST_STORE:
                break;
            case FUNC_CODE_INST_STOREATOMIC:
                break;
            case FUNC_CODE_INST_CMPXCHG:
                break;
            case FUNC_CODE_INST_LANDINGPAD:
                break;
            case FUNC_CODE_INST_CLEANUPRET:
                break;
            case FUNC_CODE_INST_CATCHRET:
                break;
            case FUNC_CODE_INST_CATCHPAD:
                break;
            case FUNC_CODE_INST_CLEANUPPAD:
                break;
            case FUNC_CODE_INST_CATCHSWITCH:
                break;
            case FUNC_CODE_OPERAND_BUNDLE:
                break;
        }

        throw new NotImplementedException(value.getCode().name());
    }

    @Override
    public RValue parseConst(PureCfgBuildingCtx ctx, Instruction value) {
        throw new NotImplementedException(value.getCode().name());
    }
}
