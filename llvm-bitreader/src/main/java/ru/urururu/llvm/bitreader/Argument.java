package ru.urururu.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Argument extends Value {
    private Argument next;

    public Argument(Type type) {
        super(type);
    }

    public void setNext(Argument next) {
        this.next = next;
    }

    public Argument getNext() {
        return next;
    }
}
