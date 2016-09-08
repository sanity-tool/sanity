package na.okutane.cpp;

import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMValueRef;
import na.okutane.cpp.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class LlvmUtils {
    public static boolean checkTag(SWIGTYPE_p_LLVMValueRef node, long tag) {
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return false;
        }
        SWIGTYPE_p_LLVMValueRef maybeTag = bitreader.LLVMGetOperand(node, 0);
        if (bitreader.LLVMIsAConstantInt(maybeTag) != null) {
            long val = bitreader.LLVMConstIntGetSExtValue(maybeTag);
            return val == tag;
        }
        return false;
    }
}
