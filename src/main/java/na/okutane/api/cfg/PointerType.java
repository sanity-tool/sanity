package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class PointerType implements Type {
    private final Type elementType;

    public PointerType(Type elementType) {
        this.elementType = elementType;
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
        throw new IllegalStateException("not struct");
    }

    @Override
    public String toString() {
        return elementType.toString() + '*';
    }
}
