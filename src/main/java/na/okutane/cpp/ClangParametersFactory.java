package na.okutane.cpp;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
public interface ClangParametersFactory {
    String[] getParameters(String filename, String objFile);
}
