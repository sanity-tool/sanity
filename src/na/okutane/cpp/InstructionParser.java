package na.okutane.cpp;

import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.UnprocessedElement;
import na.okutane.cpp.llvm.LLVMOpcode;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
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
    static {
        System.loadLibrary("irreader");
    }

    @Autowired
    SourceRangeFactory sourceRangeFactory;

    private Map<LLVMOpcode, OpcodeParser> parsers;

    private OpcodeParser defaultParser = new OpcodeParser() {
        @Override
        public LLVMOpcode getOpcode() {
            return null;
        }

        @Override
        public Cfe parse(SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseValue(SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }
    };

    @Autowired
    public InstructionParser(OpcodeParser[] parsers) {
        this.parsers = new HashMap<LLVMOpcode, OpcodeParser>();

        for (OpcodeParser parser : parsers) {
            this.parsers.put(parser.getOpcode(), parser);
        }
    }

    public Cfe parse(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        try {
            OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
            return parser.parse(instruction);
        } catch (Throwable e) {
            return new UnprocessedElement(e.getMessage(), sourceRangeFactory.getSourceRange(instruction));
        }
    }

    public RValue parseValue(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        OpcodeParser parser = parsers.getOrDefault(bitreader.LLVMGetInstructionOpcode(instruction), defaultParser);
        return parser.parseValue(instruction);
    }

    private static interface OpcodeParser {
        LLVMOpcode getOpcode();

        Cfe parse(SWIGTYPE_p_LLVMOpaqueValue instruction);

        RValue parseValue(SWIGTYPE_p_LLVMOpaqueValue instruction);
    }

    private static abstract class AbstractParser implements OpcodeParser {
        @Autowired
        SourceRangeFactory sourceRangeFactory;

        @Autowired
        ValueParser valueParser;

        @Override
        public Cfe parse(SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }

        @Override
        public RValue parseValue(SWIGTYPE_p_LLVMOpaqueValue instruction) {
            throw new IllegalStateException("opcode '" + bitreader.LLVMGetInstructionOpcode(instruction) + "' not supported");
        }
    }

    @Component
    private static class StoreParser extends AbstractParser {
        @Override
        public LLVMOpcode getOpcode() {
            return LLVMOpcode.LLVMStore;
        }

        @Override
        public Cfe parse(SWIGTYPE_p_LLVMOpaqueValue instruction) {
            return new Assignment(
                    valueParser.parseLValue(bitreader.LLVMGetOperand(instruction, 1)),
                    valueParser.parseRValue(bitreader.LLVMGetOperand(instruction, 0)),
                    sourceRangeFactory.getSourceRange(instruction)
            );
        }
    }
}
