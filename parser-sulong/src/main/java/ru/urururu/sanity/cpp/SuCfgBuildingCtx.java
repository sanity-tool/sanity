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
        if (name.startsWith("%")) {
            return "";
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

        Cfe result = labels.computeIfAbsent(block, k -> new NoOp(null));

        if (block.getInstructionCount() != 0 && block.getInstruction(0) instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) block.getInstruction(0);
            long n = phi.getSize();
            for (int i = 0; i < n; i++) {
                if (this.block.equals(phi.getBlock(i))) {
                    Assignment phiAssignment = new Assignment(
                            getOrCreateTmpVar(phi),
                            parsers.parseRValue(this, phi.getValue(i)),
                            parsers.getSourceRange(phi)
                    );
                    phiAssignment.setNext(result);

                    return phiAssignment;
                }
            }
        }

        return result;
    }

    @Override
    public LValue getOrCreateTmpVar(Instruction instruction) {
        return super.getOrCreateTmpVar(instruction, instruction.getType());
    }
}
