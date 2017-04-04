package ru.urururu.util;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class FinalReference<V> {
    private final String valueId;

    private boolean set;
    private V value;

    public FinalReference(String valueId) {
        this.valueId = valueId;
    }

    public V get() {
        if (!set) {
            throw new IllegalStateException(valueId + " not set");
        }

        return value;
    }

    public void set(V value) {
        if (set) {
            throw new IllegalStateException(valueId + " already set");
        }

        this.value = value;
        set = true;
    }

    public boolean isSet() {
        return set;
    }
}