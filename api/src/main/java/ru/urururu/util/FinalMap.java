package ru.urururu.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class FinalMap {
    public static <K, V> Map<K, V> createHashMap() {
        return new HashMap<K, V>() {
            @Override
            public V put(K key, V value) {
                return validate(key, value, super.put(key, value));
            }
        };
    }

    public static <K, V> Map<K, V> createLinkedHashMap() {
        return new LinkedHashMap<K, V>() {
            @Override
            public V put(K key, V value) {
                return validate(key, value, super.put(key, value));
            }
        };
    }

    private static <K, V> V validate(K key, V value, V oldValue) {
        if (oldValue != null) {
            // todo is it possible to support null values?
            throw new IllegalArgumentException("Tried to replace " + oldValue + " by " + value + " for" + key);
        }

        return oldValue;
    }
}