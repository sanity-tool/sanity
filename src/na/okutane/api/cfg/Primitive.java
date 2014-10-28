package na.okutane.api.cfg;

/**
* @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
*/
public class Primitive implements Type {
    @Override
    public Type getElementType() {
        return null;
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
