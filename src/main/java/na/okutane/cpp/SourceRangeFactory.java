package na.okutane.cpp;

import na.okutane.api.cfg.SourceRange;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMValueRef;
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

    public SourceRange getSourceRange(SWIGTYPE_p_LLVMValueRef instruction) {

        long id = bitreader.LLVMGetMDKindID("dbg", 3);
        SWIGTYPE_p_LLVMValueRef node = bitreader.LLVMGetMetadata(instruction, id);

        if (node != null) {
            //deepDump(node);

            SWIGTYPE_p_LLVMValueRef pair = getPair(node);

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
    }

    private SWIGTYPE_p_LLVMValueRef getPair(SWIGTYPE_p_LLVMValueRef node) {
        if (node == null) {
            return null;
        }
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return null;
        }

        if (LlvmUtils.checkTag(node, DW_TAG_file_type) || LlvmUtils.checkTag(node, DW_TAG_lexical_block)) {
            return bitreader.LLVMGetOperand(node, 1);
        } else {
            return getPair(bitreader.LLVMGetOperand(node, 2));
        }
    }
}
