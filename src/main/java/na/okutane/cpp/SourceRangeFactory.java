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
    public static final int DW_TAG_file_type = 786473;
    public static final int DW_TAG_lexical_block = 786443;
    public static final int DW_TAG_subprogram = 786478;

    private static final int FILE_INDEX = 0;
    private static final int DIRECTORY_INDEX = 1;

    public SourceRange getSourceRange(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        try {
            int line = bitreader.SAGetInstructionDebugLocLine(instruction);
            if (line != -1) {
                String filename = bitreader.SAGetInstructionDebugLocScopeFile(instruction);
                if (filename != null) {
                    return new SourceRange(filename, line);
                }
                return null;
            }

            long id = bitreader.LLVMGetMDKindID("dbg", 3);
            SWIGTYPE_p_LLVMOpaqueValue node = bitreader.LLVMGetMetadata(instruction, id);

            if (node != null) {
                //deepDump(node);

                SWIGTYPE_p_LLVMOpaqueValue pair = getPair(node);

                if (pair != null) {
                    String filename = bitreader.getMDString(bitreader.LLVMGetOperand(pair, 0));
                    String directory = bitreader.getMDString(bitreader.LLVMGetOperand(pair, 1));
                    int lineNo = (int) bitreader.LLVMConstIntGetSExtValue(bitreader.LLVMGetOperand(node, 0));
                    if (new File(filename).isAbsolute()) {
                        return new SourceRange(filename, lineNo);
                    }
                    return new SourceRange(new File(directory, filename).getAbsolutePath(), lineNo);
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private SWIGTYPE_p_LLVMOpaqueValue getPair(SWIGTYPE_p_LLVMOpaqueValue node) {
        if (node == null) {
            return null;
        }
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return null;
        }

        try {
            if (LlvmUtils.checkTag(node, DW_TAG_file_type) || LlvmUtils.checkTag(node, DW_TAG_lexical_block)) {
                return bitreader.LLVMGetOperand(node, 1);
            } else {
                return getPair(bitreader.LLVMGetOperand(node, 2));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("node = " + bitreader.LLVMPrintValueToString(node));
            int count = bitreader.LLVMGetNumOperands(node);
            System.out.println("count = " + count);
            for (int i = 0; i < count; i++) {
                System.out.println("bitreader.LLVMGetOperand(node, " + i + ") = " + bitreader.LLVMPrintValueToString(bitreader.LLVMGetOperand(node, i)));
            }
            throw e;
        }
    }
}
