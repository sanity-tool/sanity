package na.okutane.cpp;

import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.BinaryExpression;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.Indirection;
import na.okutane.api.cfg.LValue;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.UnprocessedElement;
import na.okutane.cpp.llvm.LLVMOpcode;
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

    private OpcodeParser defaultParser = new OpcodeParser() {
        @Override
        public LLVMOpcode getOpcode() {
            return null;
        }

        @Override
        public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }
    };

    @Autowired
    public InstructionParser(OpcodeParser[] parsers) {
        this.parsers = new HashMap<LLVMOpcode, OpcodeParser>();

        for (OpcodeParser parser : parsers) {
            this.parsers.put(parser.getOpcode(), parser);
        }
    }

    public Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        try {
            OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
            return parser.parse(ctx, instruction);
        } catch (Throwable e) {
            return new UnprocessedElement(e.getMessage(), sourceRangeFactory.getSourceRange(instruction));
        }
    }

    public RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction) {
        OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
        return parser.parseValue(ctx, instruction);
    }

    private static interface OpcodeParser {
        LLVMOpcode getOpcode();

        Cfe parse(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue instruction);
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
            return ctx.getTmpVar(instruction);
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
            List<RValue> args = new ArrayList<RValue>();
            int argLen = bitreader.LLVMGetNumOperands(instruction) - 1;
            SWIGTYPE_p_LLVMOpaqueValue function = bitreader.LLVMGetOperand(instruction, argLen);

            if (bitreader.LLVMIsAFunction(function) != null) {
                String name = bitreader.LLVMGetValueName(function);
                if (name.startsWith("llvm.dbg")) {
                    return null;
                }
                SWIGTYPE_p_LLVMOpaqueType type = bitreader.LLVMTypeOf(function);
                type = bitreader.LLVMGetElementType(type);
                SWIGTYPE_p_LLVMOpaqueType lvalueType = bitreader.LLVMGetReturnType(type);
                LValue lvalue = bitreader.LLVMGetTypeKind(lvalueType) == LLVMTypeKind.LLVMVoidTypeKind ? null : ctx.getTmpVar(instruction);
                for (int i = 0; i < argLen; i++) {
                    args.add(valueParser.parseRValue(ctx, bitreader.LLVMGetOperand(instruction, i)));
                }
                return new Call(
                        name,
                        lvalue,
                        args,
                        sourceRangeFactory.getSourceRange(instruction)
                );
            }

            return super.parse(ctx, instruction);
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
            LValue tmp = ctx.getTmpVar(instruction);
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
}
