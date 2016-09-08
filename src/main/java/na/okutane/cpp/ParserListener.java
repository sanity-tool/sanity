package na.okutane.cpp;

import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMModuleRef;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public interface ParserListener {
    void onModuleStarted(SWIGTYPE_p_LLVMModuleRef module);

    void onModuleFinished(SWIGTYPE_p_LLVMModuleRef module);
}
