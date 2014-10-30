package na.okutane.cpp;

import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.BinaryExpression;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.api.cfg.ConstCache;
import na.okutane.api.cfg.GetElementPointer;
import na.okutane.api.cfg.GetFieldPointer;
import na.okutane.api.cfg.IfCondition;
import na.okutane.api.cfg.Indirection;
import na.okutane.api.cfg.LValue;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.Type;
import na.okutane.api.cfg.UnprocessedElement;
import na.okutane.cpp.llvm.LLVMIntPredicate;
import na.okutane.cpp.llvm.LLVMOpcode;
import na.okutane.cpp.llvm.LLVMRealPredicate;
import na.okutane.cpp.llvm.LLVMTypeKind;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class InstructionParser {
    @Autowired
    SourceRangeFactory sourceRangeFactory;

    private Map<LLVMOpcode, OpcodeParser> parsers;

    private OpcodeParser defaultParser = new AbstractParser() {
        @Override
        public LLVMOpcode getOpcode() {
            return null;
        }
    };

    @Autowired
    public InstructionParser(OpcodeParser[] parsers) {
        this.parsers = new HashMap<>();

        for (OpcodeParser parser : parsers) {
            this.parsers.put(parser.getOpcode(), parser);
        }
    }

    public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        try {
            OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
            return parser.parse(ctx, instruction);
        } catch (Throwable e) {
            return new UnprocessedElement(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), sourceRangeFactory.getSourceRange(instruction));
        }
    }

    public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
        return parser.parseValue(ctx, instruction);
    }

    public RValue parseConst(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
        OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetConstOpcode(constant), defaultParser);
        return parser.parseConst(ctx, constant);
    }

    private static interface OpcodeParser {
        LLVMOpcode getOpcode();

        Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseConst(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant);
    }

    private static abstract class AbstractParser implements OpcodeParser {
        @Autowired
        SourceRangeFactory sourceRangeFactory;

        @Autowired
        ValueParser valueParser;

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseConst(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetConstOpcode(constant) + "' not supported");
        }
    }

    @Component
    private static class StoreParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMStore;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            SWIGTYPE_p_LLVMOpaqueValue pointer = bitreader.LLVMGetOperand(instruction, 1);
            SWIGTYPE_p_LLVMOpaqueValue value = bitreader.LLVMGetOperand(instruction, 0);
            return new Assignment(
                    new Indirection(valueParser.parseRValue(ctx, pointer)),
                    valueParser.parseRValue(ctx, value),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }
    }

    @Component
    private static class LoadParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMLoad;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return new Indirection(valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0)));
        }
    }

    @Component
    private static class RetParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMRet;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            if (bitreader.LLVMGetNumOperands(instruction) == 0) {
                return null;
            }
            return super.parse(ctx, instruction);
        }
    }

    @Component
    private static class AllocaParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMAlloca;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getOrCreateTmpVar(instruction);
        }
    }

    @Component
    private static class GetElementPtrParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMGetElementPtr;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return null;
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            RValue pointer = valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));

            int operandsCount = bitreader.LLVMGetNumOperands(instruction);

            int i = 1;
            while (i < operandsCount) {
                pointer = getPointer(pointer, valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, i)));
                i++;
            }

            return pointer;
        }

        @Override
        public RValue parseConst(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            return parseValue(ctx, constant);
        }

        private RValue getPointer(RValue basePointer, RValue index) {
            if (basePointer.getType().getElementType() != null) {
                return new GetElementPointer(basePointer, index);
            }
            if (index instanceof ConstCache.Const) {
                int intIndex = (int) ((ConstCache.Const) index).getValue();
                Type fieldType = basePointer.getType().getFieldType(intIndex);
                if (fieldType != null) {
                    return new GetFieldPointer(basePointer, intIndex);
                }
            }
            throw new IllegalStateException("can't index " + basePointer + " by " + index);
        }
    }

    @Component
    private static class CallParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMCall;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            List<RValue> args = new ArrayList<>();
            int argLen = bitreader.LLVMGetNumOperands(instruction) - 1;
            SWIGTYPE_p_LLVMOpaqueValue function = bitreader.LLVMGetOperand(instruction, argLen);

            if (bitreader.LLVMIsAConstantExpr(function) != null) {
                function = bitreader.LLVMGetOperand(function, 0);
            }
            if (bitreader.LLVMIsAFunction(function) != null) {
                String name = bitreader.LLVMGetValueName(function);
                if (name.startsWith("llvm.dbg")) {
                    return null;
                }
            }

            SWIGTYPE_p_LLVMOpaqueType type = bitreader.LLVMTypeOf(function);
            type = bitreader.LLVMGetElementType(type);
            SWIGTYPE_p_LLVMOpaqueType lvalueType = bitreader.LLVMGetReturnType(type);
            LValue lvalue = bitreader.LLVMGetTypeKind(lvalueType) == LLVMTypeKind.LLVMVoidTypeKind ? null : ctx.getOrCreateTmpVar(instruction);
            for (int i = 0; i < argLen; i++) {
                args.add(valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, i)));
            }
            return new Call(
                    valueParser.parseRValue(ctx, function),
                    lvalue,
                    args,
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    @Component
    private static class BrParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMBr;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            if (bitreader.LLVMGetNumOperands(instruction) == 1) {
                return ctx.getLabel(bitreader.LLVMGetOperand(instruction, 0));
            }
            RValue condition = valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));
            Cfe thenElement = ctx.getLabel(bitreader.LLVMGetOperand(instruction, 2));
            Cfe elseElement = ctx.getLabel(bitreader.LLVMGetOperand(instruction, 1));
            return new IfCondition(condition, thenElement, elseElement, sourceRangeFactory.getSourceRange(instruction));
        }
    }

    private static class BinaryOperationParser extends AbstractParser {
        private final LLVMOpcode opcode;
        private final BinaryExpression.Operator operator;

        public BinaryOperationParser(LLVMOpcode opcode, BinaryExpression.Operator operator) {
            this.opcode = opcode;
            this.operator = operator;
        }

        @Override
        public LLVMOpcode getOpcode() {
            return opcode;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            LValue tmp = ctx.getOrCreateTmpVar(instruction);
            return new Assignment(
                    tmp,
                    new BinaryExpression(
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0)),
                            operator,
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 1))
                    ),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    @Component
    private static class AddParser extends BinaryOperationParser {
        public AddParser() {
            super(LLVMOpcode.LLVMAdd, BinaryExpression.Operator.Add);
        }
    }

    @Component
    private static class SubParser extends BinaryOperationParser {
        public SubParser() {
            super(LLVMOpcode.LLVMSub, BinaryExpression.Operator.Sub);
        }
    }

    @Component
    private static class MulParser extends BinaryOperationParser {
        public MulParser() {
            super(LLVMOpcode.LLVMMul, BinaryExpression.Operator.Mul);
        }
    }

    @Component
    private static class DivParser extends BinaryOperationParser {
        public DivParser() {
            super(LLVMOpcode.LLVMSDiv, BinaryExpression.Operator.Div);
        }
    }

    @Component
    private static class RemParser extends BinaryOperationParser {
        public RemParser() {
            super(LLVMOpcode.LLVMSRem, BinaryExpression.Operator.Rem);
        }
    }

    @Component
    private static class AndParser extends BinaryOperationParser {
        public AndParser() {
            super(LLVMOpcode.LLVMAnd, BinaryExpression.Operator.And);
        }
    }

    @Component
    private static class OrParser extends BinaryOperationParser {
        public OrParser() {
            super(LLVMOpcode.LLVMOr, BinaryExpression.Operator.Or);
        }
    }

    @Component
    private static class XorParser extends BinaryOperationParser {
        public XorParser() {
            super(LLVMOpcode.LLVMXor, BinaryExpression.Operator.Xor);
        }
    }

    @Component
    private static class ShlParser extends BinaryOperationParser {
        public ShlParser() {
            super(LLVMOpcode.LLVMShl, BinaryExpression.Operator.ShiftLeft);
        }
    }

    @Component
    private static class ShrParser extends BinaryOperationParser {
        public ShrParser() {
            super(LLVMOpcode.LLVMAShr, BinaryExpression.Operator.ShiftRight);
        }
    }

    @Component
    private static class BitCastParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMBitCast;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            LValue tmp = ctx.getOrCreateTmpVar(instruction);
            RValue operand = valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));
            return new Assignment(
                    tmp,
                    operand,
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }

        @Override
        public RValue parseConst(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue constant) {
            return valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(constant, 0));
        }
    }

    @Component
    private static class ZExtParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMZExt;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            // todo think about source range comparison. if different - it's better to have tmp var assignment to preserve source reference.
            return null;
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            // just return it's operand, it's smaller, so it will fit.
            return valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0));
        }
    }

    @Component
    private static class ICmpParser extends AbstractParser {
        Map<LLVMIntPredicate, BinaryExpression.Operator> predicateOperatorMap = new HashMap<>();

        public ICmpParser() {
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSLT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSLE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntSGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntULT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntULE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntUGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntUGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMIntPredicate.LLVMIntNE, BinaryExpression.Operator.Ne);
        }

        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMICmp;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            LValue tmp = ctx.getOrCreateTmpVar(instruction);

            LLVMIntPredicate predicate = bitreader.LLVMGetICmpPredicate(instruction);
            BinaryExpression.Operator operator = predicateOperatorMap.get(predicate);

            if (operator == null) {
                throw new IllegalArgumentException(predicate.toString() + " not supported.");
            }

            return new Assignment(
                    tmp,
                    new BinaryExpression(
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0)),
                            operator,
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 1))
                    ),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }

    @Component
    private static class FCmpParser extends AbstractParser {
        Map<LLVMRealPredicate, BinaryExpression.Operator> predicateOperatorMap = new HashMap<>();

        public FCmpParser() {
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOLT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOLE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealULT, BinaryExpression.Operator.Lt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealULE, BinaryExpression.Operator.Le);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUGT, BinaryExpression.Operator.Gt);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUGE, BinaryExpression.Operator.Ge);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealOEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealONE, BinaryExpression.Operator.Ne);

            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUEQ, BinaryExpression.Operator.Eq);
            predicateOperatorMap.put(LLVMRealPredicate.LLVMRealUNE, BinaryExpression.Operator.Ne);
        }

        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMFCmp;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            LValue tmp = ctx.getOrCreateTmpVar(instruction);

            LLVMRealPredicate predicate = bitreader.GetFCmpPredicate(instruction);
            BinaryExpression.Operator operator = predicateOperatorMap.get(predicate);

            if (operator == null) {
                throw new IllegalArgumentException(predicate.toString() + " not supported.");
            }

            return new Assignment(
                    tmp,
                    new BinaryExpression(
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 0)),
                            operator,
                            valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, 1))
                    ),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return ctx.getTmpVar(instruction);
        }
    }
}
