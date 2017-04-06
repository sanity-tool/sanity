package ru.urururu.sanity.llvm;

import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.ParsersFacade;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.LValue;
import ru.urururu.sanity.api.cfg.Parameter;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class PureCfgBuildingCtx extends CfgBuildingCtx<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
    PureCfgBuildingCtx(ParsersFacade<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> parsers, Function function) {
        super(parsers);

        for (Value param : function.getArguments()) {
            params.put(param, new Parameter(params.size(), param.getName(), parsers.parse(param.getType())));
        }
    }

    @Override
    public LValue getOrCreateTmpVar(Instruction instruction) {
        return getOrCreateTmpVar(instruction, instruction.getType());
    }

    @Override
    public Cfe getLabel(Value label) {
        return null;
    }

    @Override
    protected String blockToString(Block block) {
        return block.toString();
    }
}
