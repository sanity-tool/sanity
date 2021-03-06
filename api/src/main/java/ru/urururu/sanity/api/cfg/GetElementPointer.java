package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class GetElementPointer implements LValue {
    private final RValue pointer;
    private final RValue index;

    public GetElementPointer(RValue pointer, RValue index) {
        this.pointer = pointer;
        this.index = index;
    }

    public RValue getPointer() {
        return pointer;
    }

    public RValue getIndex() {
        return index;
    }

    @Override
    public Type getType() {
        return pointer.getType().getElementType();
    }

    @Override
    public String toString() {
        return '(' + pointer.toString() + '+' + index.toString() + ')';
    }
}
