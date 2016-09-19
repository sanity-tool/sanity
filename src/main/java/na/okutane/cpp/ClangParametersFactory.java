package na.okutane.cpp;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface ClangParametersFactory {
    String[] getParameters(String filename, String objFile);
}
