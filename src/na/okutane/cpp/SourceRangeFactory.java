package na.okutane.cpp;

import na.okutane.api.cfg.SourceRange;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class SourceRangeFactory {
    public SourceRange getSourceRange(SWIGTYPE_p_LLVMOpaqueValue instruction) {

        long id = bitreader.LLVMGetMDKindID("dbg", 3);
        SWIGTYPE_p_LLVMOpaqueValue node = bitreader.LLVMGetMetadata(instruction, id);

        if (node != null) {
            //deepDump(node);
            String filename = getFilename(node);
            String directory = getDirectory(node);
            int lineNo = toInt(bitreader.LLVMGetOperand(node, 0));
            if (filename != null) {
                if (new File(filename).isAbsolute()) {
                    return new SourceRange(filename, lineNo);
                }
                return new SourceRange(new File(directory, filename).getAbsolutePath(), lineNo);
            }
        }
        return null;
    }

    private String getFilename(SWIGTYPE_p_LLVMOpaqueValue node) {
        SWIGTYPE_p_LLVMOpaqueValue maybeTag = bitreader.LLVMGetOperand(node, 0);
            int val = toInt(maybeTag);
            if (val == 786473) {
                SWIGTYPE_p_LLVMOpaqueValue pair = bitreader.LLVMGetOperand(node, 1);
                SWIGTYPE_p_LLVMOpaqueValue mdString = bitreader.LLVMGetOperand(pair, 0);
                return bitreader.getMDString(mdString);
            } else {
                return getFilename(bitreader.LLVMGetOperand(node, 2));
            }
    }

    private String getDirectory(SWIGTYPE_p_LLVMOpaqueValue node) {
        SWIGTYPE_p_LLVMOpaqueValue maybeTag = bitreader.LLVMGetOperand(node, 0);
            int val = toInt(maybeTag);
            if (val == 786473) {
                SWIGTYPE_p_LLVMOpaqueValue pair = bitreader.LLVMGetOperand(node, 1);
                SWIGTYPE_p_LLVMOpaqueValue mdString = bitreader.LLVMGetOperand(pair, 1);
                return bitreader.getMDString(mdString);
            } else {
                return getDirectory(bitreader.LLVMGetOperand(node, 2));
            }
    }

    private int toInt(SWIGTYPE_p_LLVMOpaqueValue value) {
        String s = bitreader.LLVMPrintValueToString(value);
        return Integer.parseInt(s.split(" ")[1], 10);
    }
}
