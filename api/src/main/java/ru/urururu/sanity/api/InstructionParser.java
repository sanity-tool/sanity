package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class InstructionParser<T, V, I, B, Ctx extends CfgBuildingCtx<T, V, I, B, Ctx>> {
    @Autowired
    protected ParsersFacade<T, V, I, B, Ctx> parsers;

    public Cfe parse(Ctx ctx, I instruction) {
        try {
            return doParse(ctx, instruction);
        } catch (Throwable e) {
            return new UnprocessedElement(e.getMessage() == null ? e.getClass().getName() : e.getMessage(), parsers.getSourceRange(instruction));
        }
    }

    protected abstract Cfe doParse(Ctx ctx, I instruction);

    protected Cfe createReturn(Ctx ctx, I instruction) {
        return null;
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
            if (name.startsWith("llvm.dbg")) {
                return null;
            }
        }

        LValue lvalue = target.getType().getReturnType().isVoid() ? null : ctx.getOrCreateTmpVar(instruction);

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
}
