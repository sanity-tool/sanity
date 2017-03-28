package ru.urururu.util;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Iterables {
    public static <E> Iterable<E> indexed(Function<Integer, E> getter, Supplier<Integer> lengthSupplier) {
        return () -> Iterators.indexed(getter, lengthSupplier);
    }

    public static <T> Iterable<T> linked(Supplier<T> first, Function<T, T> next) {
        return () -> Iterators.linked(first, next);
    }
}
