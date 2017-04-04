package ru.urururu.sanity.llvm;

import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.*;
import ru.urururu.sanity.api.cfg.Assignment;
import ru.urururu.sanity.api.cfg.CfgBuilder;
import ru.urururu.sanity.api.cfg.LValue;

import java.io.IOException;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureBytecodeParser extends AbstractBytecodeParser<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
    @Override
    protected Iterable<GlobalVariable> getGlobals(Module module) {
        return module.getGlobalVariables();
    }

    @Override
    protected void parseGlobalInitializer(CfgBuilder builder, Value initializer, LValue globalToInitialize) {
        builder.append(new Assignment(globalToInitialize, parsers.parseRValue(null, initializer), null));
    }

    @Override
    protected Value getInitializer(Value global) {
        if (!(global instanceof GlobalVariable)) {
            throw new IllegalArgumentException("global is " + global.getClass().getSimpleName());
        }
        return ((GlobalVariable)global).getInitializer();
    }

    @Override
    protected Module parseModule(String absolute) throws IOException {
        return new ModuleReader().readModule(absolute);
    }

    @Override
    protected Iterable<Function> getFunctions(Module module) {
        return module.getFunctionsWithBodies();
    }

    @Override
    protected Iterable<Block> getBlocks(Value function) {
        return ((Function)function).getBlocks();
    }

    @Override
    protected String toDebugString(Value value) {
        return value.getName();
    }

    @Override
    protected Value toValue(Block block) {
        return null;
    }

    @Override
    protected PureCfgBuildingCtx createCtx(Value function) {
        return new PureCfgBuildingCtx(parsers, (Function) function);
    }
}