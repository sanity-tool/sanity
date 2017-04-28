package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.ModelModule;
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
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.InstructionParser;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.api.cfg.Call;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.util.FinalReference;
import ru.urururu.util.Iterables;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongInstructionParser extends InstructionParser<ModelModule, com.oracle.truffle.llvm.runtime.types.Type, Symbol, Instruction, InstructionBlock, SuCfgBuildingCtx> {
    @Override
    protected Cfe doParse(SuCfgBuildingCtx ctx, Instruction instruction) {
        FinalReference<Cfe> result = new FinalReference<>("Cfe");

        instruction.accept(new InstructionVisitor() {
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
                                parsers.parseRValue(ctx, operation.getLHS()),
                                toOperator(operation.getOperator()),
                                parsers.parseRValue(ctx, operation.getRHS())
                        ),
                        parsers.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(BranchInstruction branch) {
                result.set(createGoto(ctx, branch, branch.getSuccessor()));
            }

            @Override
            public void visit(CallInstruction call) {
                visitCall(call, call.getCallTarget(), Iterables.indexed(call::getArgument, call::getArgumentCount));
            }

            @Override
            public void visit(CastInstruction cast) {
                if (cast.getOperator() == CastOperator.BITCAST) {
                    LValue tmp = ctx.getOrCreateTmpVar(instruction);
                    RValue operand = parsers.parseRValue(ctx, cast.getValue());
                    result.set(new Assignment(
                            tmp,
                            operand,
                            parsers.getSourceRange(instruction)
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
                                parsers.parseRValue(ctx, operation.getLHS()),
                                toOperator(operation.getOperator()),
                                parsers.parseRValue(ctx, operation.getRHS())
                        ),
                        parsers.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(ConditionalBranchInstruction branch) {
                result.set(createIf(ctx, branch, branch.getCondition(), branch.getTrueSuccessor(),
                        branch.getFalseSuccessor()));
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
            public void visit(CompareExchangeInstruction compareExchangeInstruction) {

            }

            @Override
            public void visit(InvokeInstruction invokeInstruction) {

            }

            @Override
            public void visit(ResumeInstruction resumeInstruction) {

            }

            @Override
            public void visit(LandingpadInstruction landingpadInstruction) {

            }

            @Override
            public void visit(VoidInvokeInstruction voidInvokeInstruction) {

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
                        new Indirection(parsers.parseRValue(ctx, store.getDestination())),
                        parsers.parseRValue(ctx, store.getSource()),
                        parsers.getSourceRange(instruction)
                ));
            }

            @Override
            public void visit(SwitchInstruction select) {
                result.set(createSwitch(ctx, select, select.getCondition(), select.getDefaultBlock(),
                        Iterables.indexed(select::getCaseValue, select.getCaseCount()),
                        Iterables.indexed(select::getCaseBlock, select.getCaseCount())));
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

                RValue function = parsers.parseRValue(ctx, callTarget);

                Type functionType = function.getType();
                if (functionType.isPointer()) {
                    functionType = functionType.getElementType();
                }
                Type returnType = functionType.getReturnType();

                LValue lvalue = returnType.isVoid() ? null : ctx.getOrCreateTmpVar(call);
                for (Symbol argument : arguments) {
                    args.add(parsers.parseRValue(ctx, argument));
                }

                result.set(new Call(
                        function,
                        lvalue,
                        args,
                        parsers.getSourceRange(instruction)
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
    public RValue parseValue(SuCfgBuildingCtx ctx, Instruction value) {
        FinalReference<RValue> result = new FinalReference<>("RValue");

        value.accept(new InstructionVisitor() {
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
                    result.set(ctx.getTmpVar(cast));
                    return;
                }

                result.set(parsers.parseRValue(ctx, cast.getValue()));
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
                RValue pointer = parsers.parseRValue(ctx, gep.getBasePointer());

                for (Symbol index : gep.getIndices()) {
                    pointer = getPointer(pointer, parsers.parseRValue(ctx, index));
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
                result.set(new Indirection(parsers.parseRValue(ctx, load.getSource())));
            }

            @Override
            public void visit(InvokeInstruction invokeInstruction) {

            }

            @Override
            public void visit(ResumeInstruction resumeInstruction) {

            }

            @Override
            public void visit(LandingpadInstruction landingpadInstruction) {

            }

            @Override
            public void visit(VoidInvokeInstruction voidInvokeInstruction) {

            }

            @Override
            public void visit(CompareExchangeInstruction compareExchangeInstruction) {

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
            throw new IllegalStateException(value.getClass().getSimpleName());
        }

        return result.get();
    }

    @Override
    public RValue parseConst(SuCfgBuildingCtx ctx, Symbol value) {
        FinalReference<RValue> result = new FinalReference<>("Constant");

        ((Constant)value).accept(new ConstantVisitor() {
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
                result.set(parsers.parseRValue(ctx, castConstant.getValue()));
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
                result.set(parsers.parseRValue(ctx, functionDeclaration));
            }

            @Override
            public void visit(FunctionDefinition functionDefinition) {
                result.set(parsers.parseRValue(ctx, functionDefinition));
            }

            @Override
            public void visit(GetElementPointerConstant getElementPointerConstant) {
                RValue pointer = parsers.parseRValue(ctx, getElementPointerConstant.getBasePointer());

                for (Symbol index : getElementPointerConstant.getIndices()) {
                    pointer = getPointer(pointer, parsers.parseRValue(ctx, index));
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
                result.set(parsers.parseRValue(ctx, stringConstant));
            }

            @Override
            public void visit(UndefinedConstant undefinedConstant) {

            }
        });

        if (!result.isSet()) {
            throw new IllegalStateException(value.getClass().getSimpleName());
        }

        return result.get();
    }
}
