package ru.urururu.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class IntegerValue extends Value {
    private final int value;

    IntegerValue(Type type, int value) {
        super(type);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
