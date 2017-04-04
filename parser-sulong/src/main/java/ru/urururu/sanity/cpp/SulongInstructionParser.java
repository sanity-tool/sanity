package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.enums.BinaryOperator;
import com.oracle.truffle.llvm.parser.model.enums.CastOperator;
import com.oracle.truffle.llvm.parser.model.enums.CompareOperator;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDeclaration;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.symbols.constants.*;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.ArrayConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.StructureConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.VectorConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.DoubleConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.FloatConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.X86FP80Constant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.BigIntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.IntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.*;
import com.oracle.truffle.llvm.parser.model.visitors.ConstantVisitor;
import com.oracle.truffle.llvm.parser.model.visitors.InstructionVisitor;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.api.cfg.Call;
import ru.urururu.sanity.api.cfg.FunctionType;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.util.FinalMap;
import ru.urururu.util.FinalReference;
import ru.urururu.util.Iterables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongInstructionParser extends InstructionParser<com.oracle.truffle.llvm.runtime.types.Type, Symbol, InstructionBlock> {
    @Autowired
    SulongValueParser valueParser; // todo to base class?

    @Override
    protected Cfe doParse(CfgBuildingCtx<com.oracle.truffle.llvm.runtime.types.Type, Symbol, InstructionBlock> ctx, Symbol instruction) {
        FinalReference<Cfe> result = new FinalReference<>("Cfe");

        ((Instruction)instruction).accept(new InstructionVisitor() {
            @Override
            public void visit(AllocateInstruction allocate) {
                result.set(null);
            }

            @Override
            public void visit(BinaryOperationInstruction operation) {
                LValue tmp = ctx.getOrCreateTmpVar(instruction);
                result.set(new Assignment(
                        tmp,
                        new BinaryExpression(
                                valueParser.parseRValue(ctx, operation.getLHS()),
                                toOperator(operation.getOperator()),
                                valueParser.parseRValue(ctx, operation.getRHS())
                        ),
                        sourceRangeFactory.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(BranchInstruction branch) {
                result.set(ctx.getLabel(branch.getSuccessor()));
            }

            @Override
            public void visit(CallInstruction call) {
                visitCall(call, call.getCallTarget(), Iterables.indexed(call::getArgument, call::getArgumentCount));
            }

            @Override
            public void visit(CastInstruction cast) {
                if (cast.getOperator() == CastOperator.BITCAST) {
                    LValue tmp = ctx.getOrCreateTmpVar(instruction);
                    RValue operand = valueParser.parseRValue(ctx, cast.getValue());
                    result.set(new Assignment(
                            tmp,
                            operand,
                            sourceRangeFactory.getSourceRange(instruction)
                    ));
                    return;
                }
                result.set(null);
            }

            @Override
            public void visit(CompareInstruction operation) {
                LValue tmp = ctx.getOrCreateTmpVar(instruction);
                result.set(new Assignment(
                        tmp,
                        new BinaryExpression(
                                valueParser.parseRValue(ctx, operation.getLHS()),
                                toOperator(operation.getOperator()),
                                valueParser.parseRValue(ctx, operation.getRHS())
                        ),
                        sourceRangeFactory.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(ConditionalBranchInstruction branch) {
                RValue condition = valueParser.parseRValue(ctx, branch.getCondition());
                Cfe thenElement = ctx.getLabel(branch.getTrueSuccessor());
                Cfe elseElement = ctx.getLabel(branch.getFalseSuccessor());
                result.set(new IfCondition(condition, thenElement, elseElement, sourceRangeFactory.getSourceRange(instruction)));
            }

            @Override
            public void visit(ExtractElementInstruction extract) {

            }

            @Override
            public void visit(ExtractValueInstruction extract) {

            }

            @Override
            public void visit(GetElementPointerInstruction gep) {
                result.set(null);
            }

            @Override
            public void visit(IndirectBranchInstruction branch) {
                throw new IllegalStateException(branch.getClass().toString());
                //result.set();//switch?
            }

            @Override
            public void visit(InsertElementInstruction insert) {

            }

            @Override
            public void visit(InsertValueInstruction insert) {

            }

            @Override
            public void visit(LoadInstruction load) {
                result.set(null);
            }

            @Override
            public void visit(PhiInstruction phi) {
                result.set(null);
            }

            @Override
            public void visit(ReturnInstruction ret) {
                if (ret.getValue() == null) {
                    result.set(null);
                }
            }

            @Override
            public void visit(SelectInstruction select) {

            }

            @Override
            public void visit(ShuffleVectorInstruction shuffle) {

            }

            @Override
            public void visit(StoreInstruction store) {
                result.set(new Assignment(
                        new Indirection(valueParser.parseRValue(ctx, store.getDestination())),
                        valueParser.parseRValue(ctx, store.getSource()),
                        sourceRangeFactory.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(SwitchInstruction select) {
                RValue controlValue = valueParser.parseRValue(ctx, select.getCondition());
                Cfe defaultCase = ctx.getLabel(select.getDefaultBlock());

                Map<RValue, Cfe> cases = FinalMap.createLinkedHashMap();

                for (int i = 0; i < select.getCaseCount(); i++) {
                    cases.put(valueParser.parseRValue(ctx, select.getCaseValue(i)), ctx.getLabel(select.getCaseBlock(i)));
                }

                result.set(new Switch(controlValue, defaultCase, cases, sourceRangeFactory.getSourceRange(instruction)));
            }

            @Override
            public void visit(SwitchOldInstruction select) {
            }

            @Override
            public void visit(UnreachableInstruction unreachable) {
            }

            @Override
            public void visit(VoidCallInstruction call) {
                visitCall(call, call.getCallTarget(), Iterables.indexed(call::getArgument, call::getArgumentCount));
            }

            private void visitCall(Instruction call, Symbol callTarget, Iterable<Symbol> arguments) {
                if (callTarget.hasName() && callTarget instanceof FunctionDeclaration) {
                    String name = ((FunctionDeclaration) callTarget).getName();
                    if (name.startsWith("@llvm.dbg")) {
                        result.set(null);
                        return;
                    }
                }

                List<RValue> args = new ArrayList<>();

                RValue function = valueParser.parseRValue(ctx, callTarget);

                Type functionType = function.getType();
                if (functionType.isPointer()) {
                    functionType = functionType.getElementType();
                }
                Type returnType = ((FunctionType) functionType).getReturnType();

                LValue lvalue = returnType.isVoid() ? null : ctx.getOrCreateTmpVar(call);
                for (Symbol argument : arguments) {
                    args.add(valueParser.parseRValue(ctx, argument));
                }

                result.set(new Call(
                        function,
                        lvalue,
                        args,
                        sourceRangeFactory.getSourceRange(instruction)
                ));
            }
        });

        if (!result.isSet()) {
            throw new IllegalStateException(instruction.getClass().getSimpleName());
        }

        return result.get();
    }

    private BinaryExpression.Operator toOperator(BinaryOperator operator) {
        switch (operator) {
            case INT_ADD:
                return BinaryExpression.Operator.Add;
            case INT_SUBTRACT:
                return BinaryExpression.Operator.Sub;
            case INT_MULTIPLY:
                return BinaryExpression.Operator.Mul;
            case INT_UNSIGNED_DIVIDE:
                return BinaryExpression.Operator.Div;
            case INT_SIGNED_DIVIDE:
                return BinaryExpression.Operator.Div;
            case INT_UNSIGNED_REMAINDER:
                return BinaryExpression.Operator.Rem;
            case INT_SIGNED_REMAINDER:
                return BinaryExpression.Operator.Rem;
            case INT_SHIFT_LEFT:
                return BinaryExpression.Operator.ShiftLeft;
            case INT_LOGICAL_SHIFT_RIGHT:
                return BinaryExpression.Operator.ShiftRight;
            case INT_ARITHMETIC_SHIFT_RIGHT:
                return BinaryExpression.Operator.ShiftRight;
            case INT_AND:
                return BinaryExpression.Operator.And;
            case INT_OR:
                return BinaryExpression.Operator.Or;
            case INT_XOR:
                return BinaryExpression.Operator.Xor;
            case FP_ADD:
                return BinaryExpression.Operator.Add;
            case FP_SUBTRACT:
                return BinaryExpression.Operator.Sub;
            case FP_MULTIPLY:
                return BinaryExpression.Operator.Mul;
            case FP_DIVIDE:
                return BinaryExpression.Operator.Div;
            case FP_REMAINDER:
                return BinaryExpression.Operator.Rem;
            default:
                throw new IllegalStateException(operator.toString());
        }
    }

    private BinaryExpression.Operator toOperator(CompareOperator operator) {
        switch (operator) {
            case FP_ORDERED_EQUAL:
                return BinaryExpression.Operator.Eq;
            case FP_ORDERED_GREATER_THAN:
                return BinaryExpression.Operator.Gt;
            case FP_ORDERED_GREATER_OR_EQUAL:
                return BinaryExpression.Operator.Ge;
            case FP_ORDERED_LESS_THAN:
                return BinaryExpression.Operator.Lt;
            case FP_ORDERED_LESS_OR_EQUAL:
                return BinaryExpression.Operator.Le;
            case FP_ORDERED_NOT_EQUAL:
                return BinaryExpression.Operator.Ne;
            case FP_UNORDERED_EQUAL:
                return BinaryExpression.Operator.Eq;
            case FP_UNORDERED_GREATER_THAN:
                return BinaryExpression.Operator.Gt;
            case FP_UNORDERED_GREATER_OR_EQUAL:
                return BinaryExpression.Operator.Ge;
            case FP_UNORDERED_LESS_THAN:
                return BinaryExpression.Operator.Lt;
            case FP_UNORDERED_LESS_OR_EQUAL:
                return BinaryExpression.Operator.Le;
            case FP_UNORDERED_NOT_EQUAL:
                return BinaryExpression.Operator.Ne;
            case INT_EQUAL:
                return BinaryExpression.Operator.Eq;
            case INT_NOT_EQUAL:
                return BinaryExpression.Operator.Ne;
            case INT_UNSIGNED_GREATER_THAN:
                return BinaryExpression.Operator.Gt;
            case INT_UNSIGNED_GREATER_OR_EQUAL:
                return BinaryExpression.Operator.Ge;
            case INT_UNSIGNED_LESS_THAN:
                return BinaryExpression.Operator.Lt;
            case INT_UNSIGNED_LESS_OR_EQUAL:
                return BinaryExpression.Operator.Le;
            case INT_SIGNED_GREATER_THAN:
                return BinaryExpression.Operator.Gt;
            case INT_SIGNED_GREATER_OR_EQUAL:
                return BinaryExpression.Operator.Ge;
            case INT_SIGNED_LESS_THAN:
                return BinaryExpression.Operator.Lt;
            case INT_SIGNED_LESS_OR_EQUAL:
                return BinaryExpression.Operator.Le;
            default:
                throw new IllegalStateException(operator.toString());
        }
    }

    @Override
    public RValue parseValue(CfgBuildingCtx<com.oracle.truffle.llvm.runtime.types.Type, Symbol, InstructionBlock> ctx, Symbol instruction) {
        FinalReference<RValue> result = new FinalReference<>("RValue");

        ((Instruction)instruction).accept(new InstructionVisitor() {
            @Override
            public void visit(AllocateInstruction allocate) {
                result.set(ctx.getOrCreateTmpVar(allocate));
            }

            @Override
            public void visit(BinaryOperationInstruction operation) {
                result.set(ctx.getTmpVar(operation));
            }

            @Override
            public void visit(BranchInstruction branch) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(CallInstruction call) {
                result.set(ctx.getTmpVar(call));
            }

            @Override
            public void visit(CastInstruction cast) {
                if (cast.getOperator() == CastOperator.BITCAST) {
                    result.set(ctx.getTmpVar(instruction));
                    return;
                }

                result.set(valueParser.parseRValue(ctx, cast.getValue()));
            }

            @Override
            public void visit(CompareInstruction operation) {
                result.set(ctx.getTmpVar(operation));
            }

            @Override
            public void visit(ConditionalBranchInstruction branch) {

            }

            @Override
            public void visit(ExtractElementInstruction extract) {

            }

            @Override
            public void visit(ExtractValueInstruction extract) {

            }

            @Override
            public void visit(GetElementPointerInstruction gep) {
                RValue pointer = valueParser.parseRValue(ctx, gep.getBasePointer());

                for (Symbol index : gep.getIndices()) {
                    pointer = getPointer(pointer, valueParser.parseRValue(ctx, index));
                }

                result.set(pointer);
            }

            @Override
            public void visit(IndirectBranchInstruction branch) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(InsertElementInstruction insert) {

            }

            @Override
            public void visit(InsertValueInstruction insert) {

            }

            @Override
            public void visit(LoadInstruction load) {
                result.set(new Indirection(valueParser.parseRValue(ctx, load.getSource())));
            }

            @Override
            public void visit(PhiInstruction phi) {
                result.set(ctx.getOrCreateTmpVar(phi));
            }

            @Override
            public void visit(ReturnInstruction ret) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(SelectInstruction select) {

            }

            @Override
            public void visit(ShuffleVectorInstruction shuffle) {

            }

            @Override
            public void visit(StoreInstruction store) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(SwitchInstruction select) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(SwitchOldInstruction select) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(UnreachableInstruction unreachable) {

            }

            @Override
            public void visit(VoidCallInstruction call) {
                throw new IllegalStateException();
            }
        });

        if (!result.isSet()) {
            throw new IllegalStateException(instruction.getClass().getSimpleName());
        }

        return result.get();
    }

    @Override
    public RValue parseConst(CfgBuildingCtx<com.oracle.truffle.llvm.runtime.types.Type, Symbol, InstructionBlock> ctx, Symbol constant) {
        FinalReference<RValue> result = new FinalReference<>("Constant");

        ((Constant)constant).accept(new ConstantVisitor() {
            @Override
            public void visit(ArrayConstant arrayConstant) {
                throw new IllegalStateException("bitreader::LLVMIsAConstantArray");
            }

            @Override
            public void visit(StructureConstant structureConstant) {
                throw new IllegalStateException("bitreader::LLVMIsAConstantStruct");
            }

            @Override
            public void visit(VectorConstant vectorConstant) {

            }

            @Override
            public void visit(BigIntegerConstant bigIntegerConstant) {

            }

            @Override
            public void visit(BinaryOperationConstant binaryOperationConstant) {

            }

            @Override
            public void visit(BlockAddressConstant blockAddressConstant) {

            }

            @Override
            public void visit(CastConstant castConstant) {
                result.set(valueParser.parseRValue(ctx, castConstant.getValue()));
            }

            @Override
            public void visit(CompareConstant compareConstant) {

            }

            @Override
            public void visit(DoubleConstant doubleConstant) {

            }

            @Override
            public void visit(FloatConstant floatConstant) {

            }

            @Override
            public void visit(X86FP80Constant x86fp80Constant) {

            }

            @Override
            public void visit(FunctionDeclaration functionDeclaration) {
                result.set(valueParser.parseRValue(ctx, functionDeclaration));
            }

            @Override
            public void visit(FunctionDefinition functionDefinition) {
                result.set(valueParser.parseRValue(ctx, functionDefinition));
            }

            @Override
            public void visit(GetElementPointerConstant getElementPointerConstant) {
                RValue pointer = valueParser.parseRValue(ctx, getElementPointerConstant.getBasePointer());

                for (Symbol index : getElementPointerConstant.getIndices()) {
                    pointer = getPointer(pointer, valueParser.parseRValue(ctx, index));
                }

                result.set(pointer);
            }

            @Override
            public void visit(InlineAsmConstant inlineAsmConstant) {

            }

            @Override
            public void visit(IntegerConstant integerConstant) {

            }

            @Override
            public void visit(NullConstant nullConstant) {

            }

            @Override
            public void visit(StringConstant stringConstant) {
                result.set(valueParser.parseRValue(ctx, stringConstant));
            }

            @Override
            public void visit(UndefinedConstant undefinedConstant) {

            }
        });

        if (!result.isSet()) {
            throw new IllegalStateException(constant.getClass().getSimpleName());
        }

        return result.get();
    }
}
