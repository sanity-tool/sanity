package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.functions.FunctionParameter;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.PhiInstruction;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.util.Iterables;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SuCfgBuildingCtx extends CfgBuildingCtx<ModelModule, com.oracle.truffle.llvm.runtime.types.Type, Symbol, Instruction, InstructionBlock, SuCfgBuildingCtx> {
    SuCfgBuildingCtx(SulongParsersFacade parsers, FunctionDefinition function) {
        super(parsers);

        for (FunctionParameter parameter : function.getParameters()) {
            params.put(parameter, new Parameter(params.size(), defaultName(parameter.getName()), getType(parameter)));
        }
    }

    private String defaultName(String name) {
        System.out.println("name = [" + name + "]");
        if (name.startsWith("%")) {
            return name.substring(1);
        }
        return name;
    }

    protected Type getType(Symbol value) {
        return parsers.parse(value.getType());
    }

    private InstructionBlock getValueAsBasicBlock(Symbol label) {
        return (InstructionBlock)label;
    }

    @Override
    public Cfe getLabel(Symbol label) {
        InstructionBlock block = getValueAsBasicBlock(label);

        Cfe result = getBlockEntrance(block);

        if (block.getInstructionCount() != 0 && block.getInstruction(0) instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) block.getInstruction(0);

            int n = phi.getSize();
            return prependPhiAssignment(phi, result, Iterables.indexed(phi::getBlock, n), Iterables.indexed(phi::getValue, n));
        }

        return result;
    }

    @Override
    protected String blockToString(InstructionBlock block) {
        return block.toString();
    }

    @Override
    public LValue getOrCreateTmpVar(Instruction instruction) {
        return super.getOrCreateTmpVar(instruction, instruction.getType());
    }
}
