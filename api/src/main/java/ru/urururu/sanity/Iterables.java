package ru.urururu.sanity;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Iterables {
    public static <T> Iterable<T> fromFunctions(Supplier<T> first, Function<T, T> next) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    T item = first.get();

                    @Override
                    public boolean hasNext() {
                        return item != null;
                    }

                    @Override
                    public T next() {
                        if (item == null) {
                            throw new NoSuchElementException();
                        }

                        T result = item;

                        item = next.apply(item);

                        return result;
                    }
                };
            }
        };
    }
}
