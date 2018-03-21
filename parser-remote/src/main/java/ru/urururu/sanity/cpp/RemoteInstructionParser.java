package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.InstructionDto;
import io.swagger.client.model.ValueRefDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.InstructionParser;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.util.FinalMap;

import java.util.*;


/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteInstructionParser extends InstructionParser<Integer,
        ValueRefDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> {
    @Autowired
    private ConstCache constants;

    private Map<String, OpcodeParser> opcodeParsers;

    private OpcodeParser defaultParser = new AbstractParser() {
        @Override
        public Set<String> getOpcodes() {
            return Collections.emptySet();
        }
    };

    public RemoteInstructionParser() {
        this.opcodeParsers = FinalMap.createHashMap();

        List<OpcodeParser> parsersOtu = new ArrayList<>();
        parsersOtu.add(new StoreParser());
        parsersOtu.add(new LoadParser());
        parsersOtu.add(new RetParser());
        parsersOtu.add(new AllocaParser());
        parsersOtu.add(new GetElementPtrParser());
        parsersOtu.add(new ExtractValueParser());
        parsersOtu.add(new CallParser());
        parsersOtu.add(new PhiParser());
        parsersOtu.add(new BrParser());
        parsersOtu.add(new SwitchParser());
        parsersOtu.add(new BinaryOperationParser());
        parsersOtu.add(new BitCastParser());
        parsersOtu.add(new CastParser());

        parsersOtu.add(new ICmpParser());
        parsersOtu.add(new FCmpParser());

        for (OpcodeParser parser : parsersOtu) {
            for (String opcode : parser.getOpcodes()) {
                opcodeParsers.put(opcode, parser);
            }
        }
    }

    protected Cfe doParse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
        OpcodeParser parser = opcodeParsers.getOrDefault(instruction.getKind(), defaultParser);

        return parser.parse(ctx, instruction);
    }

    public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
        OpcodeParser parser = opcodeParsers.getOrDefault(instruction.getKind(), defaultParser);
        return parser.parseValue(ctx, instruction);
    }

    public RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant) {
        OpcodeParser parser = opcodeParsers.getOrDefault(constant.getKind()/*todo bitreader.LLVMGetConstOpcode(constant)*/, defaultParser);
        return parser.parseConst(ctx, constant);
    }

    private interface OpcodeParser {
        Set<String> getOpcodes();

        Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction);

        RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction);

        RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant);
    }

    private static abstract class AbstractParser implements OpcodeParser {
        @Override
        public Set<String> getOpcodes() {
            return Collections.singleton(getOpcode());
        }

        public String getOpcode() {
            return null;
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            throw new IllegalStateException("opcode '" + instruction.getKind() + "' not supported");
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            throw new IllegalStateException("opcode '" + instruction.getKind() + "' not supported");
        }

        @Override
        public RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant) {
            throw new IllegalStateException("opcode '" + constant.getKind() + "' not supported");
        }
    }

    private class StoreParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMStore";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createStore(ctx, instruction,
                    instruction.getOperands().get(0), instruction.getOperands().get(1));
        }
    }

    private class LoadParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMLoad";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createLoad(ctx, instruction.getOperands().get(0));
        }
    }

    private class RetParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMRet";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            if (instruction.getOperands().size() == 0) {
                return createReturn(ctx, instruction);
            }

            return createReturn(ctx, instruction, instruction.getOperands().get(0));
        }
    }

    private static class AllocaParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMAlloca";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getOrCreateTmpVar(instruction);
        }
    }

    private class GetElementPtrParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMGetElementPtr";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return getPointer(ctx, instruction.getOperands().get(0),
                    instruction.getOperands().subList(1, instruction.getOperands().size()));
        }

        @Override
        public RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant) {
            return parseValue(ctx, constant);
        }
    }

    private class ExtractValueParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMExtractValue";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            // todo wtf? tests?
            RValue pointer = parsers.parseRValue(ctx, instruction.getOperands().get(0));

            pointer = getPointer(pointer, constants.get(0, null));

            int operandsCount = instruction.getOperands().size();

            int i = 1;
            while (i < operandsCount) {
                pointer = getPointer(pointer, parsers.parseRValue(ctx, instruction.getOperands().get(i)));
                i++;
            }

            return pointer;
        }

        @Override
        public RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant) {
            return parseValue(ctx, constant);
        }
    }

    private class CallParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMCall";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createCall(ctx, instruction, instruction.getOperands().get(instruction.getOperands().size() - 1),
                    instruction.getOperands().subList(0, instruction.getOperands().size() - 1));
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class PhiParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMPHI";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class BrParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMBr";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            if (instruction.getOperands().size() == 1) {
                return createGoto(ctx, instruction, instruction.getOperands().get(0));
            }
            return createIf(ctx, instruction, instruction.getOperands().get(0),
                    instruction.getOperands().get(2), instruction.getOperands().get(1));
        }
    }

    private class SwitchParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMSwitch";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            List<ValueRefDto> values = new ArrayList<>();
            List<ValueRefDto> labels = new ArrayList<>();

            int i = 2;
            while (i + 1 < instruction.getOperands().size()) {
                values.add(instruction.getOperands().get(i++));
                labels.add(instruction.getOperands().get(i++));
            }

            return createSwitch(ctx, instruction, instruction.getOperands().get(0), instruction.getOperands().get(1), values, labels);
        }
    }

    private class BinaryOperationParser extends AbstractParser {
        private final Map<String, BinaryExpression.Operator> opcodeOperatorMap;

        BinaryOperationParser() {
            opcodeOperatorMap = FinalMap.createHashMap();
            opcodeOperatorMap.put("LLVMAdd", BinaryExpression.Operator.Add);
            opcodeOperatorMap.put("LLVMFAdd", BinaryExpression.Operator.Add);
            opcodeOperatorMap.put("LLVMSub", BinaryExpression.Operator.Sub);
            opcodeOperatorMap.put("LLVMFSub", BinaryExpression.Operator.Sub);
            opcodeOperatorMap.put("LLVMMul", BinaryExpression.Operator.Mul);
            opcodeOperatorMap.put("LLVMFMul", BinaryExpression.Operator.Mul);
            opcodeOperatorMap.put("LLVMUDiv", BinaryExpression.Operator.Div);
            opcodeOperatorMap.put("LLVMSDiv", BinaryExpression.Operator.Div);
            opcodeOperatorMap.put("LLVMFDiv", BinaryExpression.Operator.Div);
            opcodeOperatorMap.put("LLVMURem", BinaryExpression.Operator.Rem);
            opcodeOperatorMap.put("LLVMSRem", BinaryExpression.Operator.Rem);
            opcodeOperatorMap.put("LLVMFRem", BinaryExpression.Operator.Rem);

            opcodeOperatorMap.put("LLVMAnd", BinaryExpression.Operator.And);
            opcodeOperatorMap.put("LLVMOr", BinaryExpression.Operator.Or);
            opcodeOperatorMap.put("LLVMXor", BinaryExpression.Operator.Xor);

            opcodeOperatorMap.put("LLVMShl", BinaryExpression.Operator.ShiftLeft);
            opcodeOperatorMap.put("LLVMLShr", BinaryExpression.Operator.ShiftRight);
            opcodeOperatorMap.put("LLVMAShr", BinaryExpression.Operator.ShiftRight);
        }

        @Deprecated
        public BinaryOperationParser(String opcode, BinaryExpression.Operator operator) {
            opcodeOperatorMap = Collections.singletonMap(opcode, operator);
        }

        @Override
        public Set<String> getOpcodes() {
            return opcodeOperatorMap.keySet();
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createBinaryAssignment(ctx, instruction,
                    instruction.getOperands().get(0),
                    opcodeOperatorMap.get(instruction.getKind()),
                    instruction.getOperands().get(1));
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class BitCastParser extends AbstractParser {
        @Override
        public String getOpcode() {
            return "LLVMBitCast";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createBitCast(ctx, instruction, instruction.getOperands().get(0));
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }

        @Override
        public RValue parseConst(RemoteCfgBuildingCtx ctx, InstructionDto constant) {
            return parsers.parseRValue(ctx, constant.getOperands().get(0));
        }
    }

    private class CastParser extends AbstractParser {
        @Override
        public Set<String> getOpcodes() {
            return new HashSet<>(Arrays.asList("LLVMSExt", "LLVMZExt", "LLVMTrunc", "LLVMSIToFP"));
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            // todo think about source range comparison. if different - it's better to have tmp var assignment to preserve source reference.
            return null;
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            // just return it's operand, it's smaller, so it will fit.
            return parsers.parseRValue(ctx, instruction.getOperands().get(0));
        }
    }

    private class ICmpParser extends AbstractParser {
        Map<String, BinaryExpression.Operator> predicateOperatorMap = FinalMap.createHashMap();

        ICmpParser() {
            predicateOperatorMap.put("LLVMIntSLT", BinaryExpression.Operator.Lt);
            predicateOperatorMap.put("LLVMIntSLE", BinaryExpression.Operator.Le);
            predicateOperatorMap.put("LLVMIntSGT", BinaryExpression.Operator.Gt);
            predicateOperatorMap.put("LLVMIntSGE", BinaryExpression.Operator.Ge);

            predicateOperatorMap.put("LLVMIntULT", BinaryExpression.Operator.Lt);
            predicateOperatorMap.put("LLVMIntULE", BinaryExpression.Operator.Le);
            predicateOperatorMap.put("LLVMIntUGT", BinaryExpression.Operator.Gt);
            predicateOperatorMap.put("LLVMIntUGE", BinaryExpression.Operator.Ge);

            predicateOperatorMap.put("LLVMIntEQ", BinaryExpression.Operator.Eq);
            predicateOperatorMap.put("LLVMIntNE", BinaryExpression.Operator.Ne);
        }

        @Override
        public String getOpcode() {
            return "LLVMICmp";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createBinaryAssignment(ctx, instruction,
                    instruction.getOperands().get(0),
                    predicateOperatorMap.get("LLVMIntUGT"),
                    instruction.getOperands().get(1));
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    private class FCmpParser extends AbstractParser {
        Map<String, BinaryExpression.Operator> predicateOperatorMap = FinalMap.createHashMap();

        FCmpParser() {
            predicateOperatorMap.put("LLVMRealOLT", BinaryExpression.Operator.Lt);
            predicateOperatorMap.put("LLVMRealOLE", BinaryExpression.Operator.Le);
            predicateOperatorMap.put("LLVMRealOGT", BinaryExpression.Operator.Gt);
            predicateOperatorMap.put("LLVMRealOGE", BinaryExpression.Operator.Ge);

            predicateOperatorMap.put("LLVMRealULT", BinaryExpression.Operator.Lt);
            predicateOperatorMap.put("LLVMRealULE", BinaryExpression.Operator.Le);
            predicateOperatorMap.put("LLVMRealUGT", BinaryExpression.Operator.Gt);
            predicateOperatorMap.put("LLVMRealUGE", BinaryExpression.Operator.Ge);

            predicateOperatorMap.put("LLVMRealOEQ", BinaryExpression.Operator.Eq);
            predicateOperatorMap.put("LLVMRealONE", BinaryExpression.Operator.Ne);

            predicateOperatorMap.put("LLVMRealUEQ", BinaryExpression.Operator.Eq);
            predicateOperatorMap.put("LLVMRealUNE", BinaryExpression.Operator.Ne);
        }

        @Override
        public String getOpcode() {
            return "LLVMFCmp";
        }

        @Override
        public Cfe parse(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return createBinaryAssignment(ctx, instruction,
                    instruction.getOperands().get(0),
                    predicateOperatorMap.get("LLVMRealOLT"),
                    instruction.getOperands().get(1));
        }

        @Override
        public RValue parseValue(RemoteCfgBuildingCtx ctx, InstructionDto instruction) {
            return ctx.getTmpVar(instruction);
        }
    }
}
