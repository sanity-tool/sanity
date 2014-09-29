package na.okutane.cpp;

import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.UnprocessedElement;
import na.okutane.cpp.llvm.Instruction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class InstructionParser {
    @Autowired
    SourceRangeFactory sourceRangeFactory;

    private Map<String, OpcodeParser> parsers;

    @Autowired
    public InstructionParser(OpcodeParser[] parsers) {
        this.parsers = new HashMap<String, OpcodeParser>();

        for (OpcodeParser parser : parsers) {
            this.parsers.put(parser.getOpcodeName(), parser);
        }
    }

    public Cfe parse(Instruction instruction) {
        OpcodeParser parser = parsers.get(instruction.getOpcodeName());

        if (parser != null) {
            return parser.parse(instruction);
        }

        return new UnprocessedElement("opcode '" + instruction.getOpcodeName() + "' not supported", sourceRangeFactory.getSourceRange(instruction));
    }

    private static interface OpcodeParser {
        String getOpcodeName();
        Cfe parse(Instruction instruction);
    }

    @Component
    private static class DummyParser implements OpcodeParser {// todo not needed, just want to make spring happy.
        @Override
        public String getOpcodeName() {
            return "dummy";
        }

        @Override
        public Cfe parse(Instruction instruction) {
            return null;
        }
    }
}
