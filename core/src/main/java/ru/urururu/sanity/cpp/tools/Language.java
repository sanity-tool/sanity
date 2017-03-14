package ru.urururu.sanity.cpp.tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public enum Language {
    C("c"),
    Cpp("cpp"),
    ObjectiveC("m"),
    Swift("swift"),
    IR("ll"),
    ;

    Set<String> extensions;

    Language(String... extensions) {
        this.extensions = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(extensions)));
    }

    public Set<String> getExtensions() {
        return extensions;
    }
}
