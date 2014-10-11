package na.okutane.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class Indirection implements LValue {
    private final RValue pointer;

    public Indirection(RValue pointer) {
        this.pointer = pointer;
    }

    public RValue getPointer() {
        return pointer;
    }
}
