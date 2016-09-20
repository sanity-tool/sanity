package ru.urururu.sanity.cpp;

import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class LlvmUtils {
    public static boolean checkTag(SWIGTYPE_p_LLVMOpaqueValue node, long tag) {
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return false;
        }
        SWIGTYPE_p_LLVMOpaqueValue maybeTag = bitreader.LLVMGetOperand(node, 0);
        if (bitreader.LLVMIsAConstantInt(maybeTag) != null) {
            long val = bitreader.LLVMConstIntGetSExtValue(maybeTag);
            return val == tag;
        }
        return false;
    }
}
