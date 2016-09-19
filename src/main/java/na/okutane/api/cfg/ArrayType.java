package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ArrayType implements Type {
    private final Type elementType;
    private final long length;

    public ArrayType(Type elementType, long length) {
        this.elementType = elementType;
        this.length = length;
    }

    @Override
    public Type getElementType() {
        return elementType;
    }

    @Override
    public Type getFieldType(int index) {
        return null;
    }

    @Override
    public String getFieldName(int index) {
        return null;
    }
}
