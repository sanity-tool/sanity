package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RealConst extends TypedValue implements Value {
    private final double value;

    public RealConst(double value, Type type) {
        super(type);
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }
}
