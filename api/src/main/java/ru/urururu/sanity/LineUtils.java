package ru.urururu.sanity;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class LineUtils implements DisposableBean {
    private static final Map<Pair<File, Integer>, String> CACHE = new HashMap<>();

    public static String dumpLine(File file, int lineNumber) {
        return CACHE.computeIfAbsent(Pair.of(file, lineNumber), key -> {
            try (FileReader in = new FileReader(file);
                 LineNumberReader reader = new LineNumberReader(in)) {
                String line;
                do {
                    line = reader.readLine();
                } while (reader.getLineNumber() < lineNumber);
                return line.trim();
            } catch (IOException e) {
                throw new IllegalStateException("couldn't dump " + lineNumber + " line of " + file, e);
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        CACHE.clear();
    }
}
