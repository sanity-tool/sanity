package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.functions.FunctionParameter;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.PhiInstruction;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import ru.urururu.sanity.api.cfg.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SuCfgBuildingCtx extends CfgBuildingCtx<com.oracle.truffle.llvm.runtime.types.Type, Symbol, InstructionBlock> {
    private final SulongParsersFacade parsers;

    SuCfgBuildingCtx(SulongParsersFacade parsers, FunctionDefinition function) {
        this.parsers = parsers;

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

    @Override
    protected Type getType(Symbol value) {
        return parsers.parse(value.getType());
    }

    @Override
    protected InstructionBlock getValueAsBasicBlock(Symbol label) {
        return (InstructionBlock)label;
    }

    @Override
    public Cfe getLabel(Symbol label) {
        Cfe result = super.getLabel(label);

        InstructionBlock referencedBlock = getValueAsBasicBlock(label);
        if (referencedBlock.getInstructionCount() != 0 && referencedBlock.getInstruction(0) instanceof PhiInstruction) {
            PhiInstruction phi = (PhiInstruction) referencedBlock.getInstruction(0);
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
}
