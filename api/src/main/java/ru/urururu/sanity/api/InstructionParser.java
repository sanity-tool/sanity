package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.util.FinalMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class InstructionParser<T, V, I, B, Ctx extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    @Autowired
    protected ParsersFacade<T, V, I, B, Ctx> parsers;

    public Cfe parse(Ctx ctx, I instruction) {
        try {
            return doParse(ctx, instruction);
        } catch (Exception e) {
            return new UnprocessedElement(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), parsers.getSourceRange(instruction));
        }
    }

    protected abstract Cfe doParse(Ctx ctx, I instruction);

    protected Cfe createAllocation(Ctx ctx, LocalVar local, I instruction) {
        return new Allocation(local, parsers.getSourceRange(instruction));
    }

    protected Cfe createReturn(Ctx ctx, I instruction) {
        return new Return(null, parsers.getSourceRange(instruction));
    }

    protected Cfe createReturn(Ctx ctx, I instruction, V value) {
        return new Return(parsers.parseRValue(ctx, value), parsers.getSourceRange(instruction));
    }

    protected Cfe createStore(Ctx ctx, I instruction, V value, V pointer) {
        return new Assignment(
                new Indirection(parsers.parseRValue(ctx, pointer)),
                parsers.parseRValue(ctx, value),
                parsers.getSourceRange(instruction)
        );
    }

    protected Cfe createCall(Ctx ctx, I instruction, V function, Iterable<V> parameters) {
        RValue target = parsers.parseRValue(ctx, function);

        if (target instanceof FunctionAddress) {
            String name = ((FunctionAddress) target).getName();
            if (name.equals("llvm.dbg.declare")) {
                LocalVar local = ctx.getOrCreateLocalVar((I) parameters.iterator().next());
                local.setAllocationRange(parsers.getSourceRange(instruction));
            }
            if (name.startsWith("llvm.dbg")) {
                return null;
            }
        }

        Type returnType;
        if (target.getType().getElementType() == null) {
            returnType = target.getType().getReturnType(); // is this correct?
        } else {
            returnType = target.getType().getElementType().getReturnType();
        }

        LValue lvalue = returnType.isVoid() ? null : ctx.getOrCreateTmpVar(instruction);

        List<RValue> args = new ArrayList<>();
        for (V param : parameters) {
            args.add(parsers.parseRValue(ctx, param));
        }

        return new Call(
                target,
                lvalue,
                args,
                parsers.getSourceRange(instruction)
        );
    }

    protected Cfe createBinaryAssignment(Ctx ctx, I instruction, V left, BinaryExpression.Operator operator, V right) {
        return new Assignment(
                ctx.getOrCreateTmpVar(instruction),
                new BinaryExpression(
                        parsers.parseRValue(ctx, left),
                        operator,
                        parsers.parseRValue(ctx, right)
                ),
                parsers.getSourceRange(instruction)
        );
    }

    protected RValue getPointer(Ctx ctx, V basePointer, Iterable<V> indices) {
        RValue pointer = parsers.parseRValue(ctx, basePointer);

        for (V index : indices) {
            pointer = getPointer(pointer, parsers.parseRValue(ctx, index));
        }

        return pointer;
    }

    protected RValue getPointer(RValue basePointer, RValue index) {
        if (basePointer.getType().getElementType() != null) {
            return new GetElementPointer(basePointer, index);
        }
        if (index instanceof Const) {
            int intIndex = (int) ((Const) index).getValue();
            Type fieldType = basePointer.getType().getFieldType(intIndex);
            if (fieldType != null) {
                return new GetFieldPointer(basePointer, intIndex);
            }
        }
        throw new IllegalStateException("can't index " + CfePrinter.DEFAULT.printValue(basePointer) + " by " + index);
    }

    protected Cfe createSwitch(Ctx ctx, I instruction, V controlValue, V defaultCase, Iterable<V> values, Iterable<V> labels) {
        Map<RValue, Cfe> cases = FinalMap.createLinkedHashMap();

        Iterator<V> valuesIterator = values.iterator();
        Iterator<V> labelsIterator = labels.iterator();
        while (valuesIterator.hasNext() && labelsIterator.hasNext()) {
            cases.put(parsers.parseRValue(ctx, valuesIterator.next()), ctx.getLabel(labelsIterator.next()));
        }

        if (valuesIterator.hasNext()) {
            throw new IllegalStateException("out of labels, but have value");
        }
        if (labelsIterator.hasNext()) {
            throw new IllegalStateException("out of values, but have label");
        }

        return new Switch(parsers.parseRValue(ctx, controlValue), ctx.getLabel(defaultCase), cases, parsers.getSourceRange(instruction));
    }

    protected Cfe createGoto(Ctx ctx, I instruction, V label) {
        return ctx.getLabel(label);
    }

    protected Cfe createIf(Ctx ctx, I instruction, V condition, V thenElement, V elseElement) {
        return new IfCondition(parsers.parseRValue(ctx, condition),
                ctx.getLabel(thenElement), ctx.getLabel(elseElement), parsers.getSourceRange(instruction));
    }

    protected Cfe createBitCast(Ctx ctx, I instruction, V value) {
        LValue tmp = ctx.getOrCreateTmpVar(instruction);
        RValue operand = parsers.parseRValue(ctx, value);
        return new Assignment(
                tmp,
                operand,
                parsers.getSourceRange(instruction)
        );
    }

    public abstract RValue parseValue(Ctx ctx, I value);

    public abstract RValue parseConst(Ctx ctx, I value);

    public RValue createLoad(Ctx ctx, V value) {
        return new Indirection(parsers.parseRValue(ctx, value));
    }
}
