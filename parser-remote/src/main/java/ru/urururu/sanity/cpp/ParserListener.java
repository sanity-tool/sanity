package ru.urururu.sanity.cpp;

import io.swagger.client.model.ModuleDto;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ParserListener {
    default void onModuleStarted(ModuleDto module) {
    }

    default void onModuleFinished(ModuleDto module) {
    }
}
