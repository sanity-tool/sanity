package na.okutane.cpp;

import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.UnprocessedElement;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.StoreInst;
import na.okutane.cpp.llvm.bitreader;
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

    private OpcodeParser defaultParser = new OpcodeParser() {
        @Override
        public String getOpcodeName() {
            return null;
        }

        @Override
        public Cfe parse(Instruction instruction) {
            throw new IllegalStateException("opcode '" + instruction.getOpcodeName() + "' not supported");
        }

        @Override
        public RValue parseValue(Instruction instruction) {
            throw new IllegalStateException("opcode '" + instruction.getOpcodeName() + "' not supported");
        }
    };

    @Autowired
    public InstructionParser(OpcodeParser[] parsers) {
        this.parsers = new HashMap<String, OpcodeParser>();

        for (OpcodeParser parser : parsers) {
            this.parsers.put(parser.getOpcodeName(), parser);
        }
    }

    public Cfe parse(Instruction instruction) {
        try {
            OpcodeParser parser = parsers.getOrDefault(instruction.getOpcodeName(), defaultParser);
            return parser.parse(instruction);
        } catch (Throwable e) {
            return new UnprocessedElement(e.getMessage(), sourceRangeFactory.getSourceRange(instruction));
        }
    }

    public RValue parseValue(Instruction instruction) {
        OpcodeParser parser = parsers.getOrDefault(instruction.getOpcodeName(), defaultParser);
        return parser.parseValue(instruction);
    }

    private static interface OpcodeParser {
        String getOpcodeName();

        Cfe parse(Instruction instruction);

        RValue parseValue(Instruction instruction);
    }

    private static abstract class AbstractParser<K extends Instruction> implements OpcodeParser {
        @Autowired
        SourceRangeFactory sourceRangeFactory;

        @Autowired
        ValueParser valueParser;

        protected abstract K convert(Instruction instruction);

        protected Cfe parse0(K instruction) {
            throw new IllegalStateException("opcode '" + instruction.getOpcodeName() + "' not supported");
        }

        private RValue parseValue0(K instruction) {
            throw new IllegalStateException("opcode '" + instruction.getOpcodeName() + "' not supported");
        }

        @Override
        public final Cfe parse(Instruction instruction) {
            return parse0(convert(instruction));
        }

        @Override
        public RValue parseValue(Instruction instruction) {
            return parseValue0(convert(instruction));
        }
    }

    @Component
    private static class StoreInstructionParser extends AbstractParser<StoreInst> {
        @Override
        public String getOpcodeName() {
            return "store";
        }

        @Override
        protected StoreInst convert(Instruction instruction) {
            return bitreader.toStoreInst(instruction);
        }

        @Override
        protected Cfe parse0(StoreInst instruction) {
            return new Assignment(
                    valueParser.parseLValue(instruction.getPointerOperand()),
                    valueParser.parseRValue(instruction.getValueOperand()),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }
    }

    @Component
    private static class AddInstructionParser extends AbstractParser<Instruction> {
        @Override
        public String getOpcodeName() {
            return "add";
        }

        @Override
        protected Instruction convert(Instruction instruction) {
            return instruction;
        }

        @Override
        protected Cfe parse0(Instruction instruction) {
            return null;
        }
    }
}
