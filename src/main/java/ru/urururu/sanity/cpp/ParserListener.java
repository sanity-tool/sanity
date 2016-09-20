package ru.urururu.sanity.cpp;

import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ParserListener {
    void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module);

    void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module);
}
