package ru.urururu.sanity.api;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ParserListener<M> {
    default void onModuleStarted(M module) {
    }

    default void onModuleFinished(M module) {
    }
}
