package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class GetFieldPointer implements LValue {
    private final RValue pointer;
    private final int index;

    public GetFieldPointer(RValue pointer, int index) {
        this.pointer = pointer;
        this.index = index;
    }

    public RValue getPointer() {
        return pointer;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        return pointer.getType().getFieldType(index);
    }

    @Override
    public String toString() {
        return '(' + pointer.toString() + '.' + pointer.getType().getFieldName(index) + ')';
    }
}
