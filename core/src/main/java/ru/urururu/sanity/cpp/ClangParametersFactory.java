package ru.urururu.sanity.cpp;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ClangParametersFactory {
    String[] getParameters(String filename, String objFile);

    String[] getDebugParameters(String filename, String debugFile);
}
