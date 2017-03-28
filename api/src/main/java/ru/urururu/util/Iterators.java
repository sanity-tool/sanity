package ru.urururu.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Iterators {
    public static <E> Iterator<E> indexed(Function<Integer, E> getter, Supplier<Integer> lengthSupplier) {
        return new Iterator<E>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < lengthSupplier.get();
            }

            @Override
            public E next() {
                return getter.apply(i++);
            }
        };
    }

    public static <T> Iterator<T> linked(Supplier<T> first, Function<T, T> next) {
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
}