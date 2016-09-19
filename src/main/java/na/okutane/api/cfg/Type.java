package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Type {
    Type getElementType();

    Type getFieldType(int index);

    String getFieldName(int index);
}
