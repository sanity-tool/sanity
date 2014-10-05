package na.okutane.cpp;

import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.LValue;
import na.okutane.api.cfg.RValue;
import na.okutane.cpp.llvm.GlobalValue;
import na.okutane.cpp.llvm.GlobalVariable;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.Value;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class ValueParser {
    @Autowired
    GlobalVariableCache globals;
    @Autowired
    InstructionParser instructionParser;

    public LValue parseLValue(Value value) {
        if (GlobalVariable.classof(value)) {
            //GlobalVariable var = bitreader.toGlobalVariable(value);
            return globals.get(value.getName().begin());
        }

        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    public RValue parseRValue(Value value) {
        if (Instruction.classof(value)) {
            Instruction inst = bitreader.toInstruction(value);

            return instructionParser.parseValue(inst);
        }
        throw new IllegalStateException("Can't parse RValue: " + value.getValueID());
    }
}
