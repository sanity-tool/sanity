package ru.urururu.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class GlobalVariable extends Value {
    private Value initializer;
    private GlobalVariable next;

    GlobalVariable(Type type) {
        super(type);
    }

    void setNext(GlobalVariable next) {
        this.next = next;
    }

    public GlobalVariable getNext() {
        return next;
    }

    public Value getInitializer() {
        return initializer;
    }

    void setInitializer(Value initializer) {
        this.initializer = initializer;
    }
}
