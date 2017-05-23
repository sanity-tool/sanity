package ru.urururu.util;

import org.apache.commons.lang3.StringUtils;
import ru.urururu.sanity.api.cfg.SourceRange;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Coverage {
    private static final Map<File, List<Integer>> coverage = new HashMap<>();

    private Coverage() {
        // prevent creation
    }

    public static void markAsCode(SourceRange sourceRange) {
        markAsCode(sourceRange.getFile(), sourceRange.getLine() - 1);
    }

    public static void hit(SourceRange sourceRange) {
        hit(sourceRange.getFile(), sourceRange.getLine() - 1);
    }

    private static synchronized void markAsCode(File file, int line) {
        List<Integer> coverageInfo = coverage.computeIfAbsent(file, __ -> new ArrayList<>());
        while (coverageInfo.size() < line + 1) {
            coverageInfo.add(null);
        }

        Integer oldInfo = coverageInfo.get(line);
        coverageInfo.set(line, oldInfo == null ? 0 : oldInfo);
    }

    private static synchronized void hit(File file, int line) {
        List<Integer> coverageInfo = coverage.get(file);

        if (coverageInfo.size() <= line) {
            throw new IndexOutOfBoundsException("Line: " + line + ", Size: " + coverageInfo.size());
        }

        Integer integer = coverageInfo.get(line);

        if (integer == null) {
            throw new IllegalStateException("Not marked as code. " + "Line: " + line + ", Size: " + coverageInfo.size());
        }

        coverageInfo.set(line, integer + 1);
    }

    public static synchronized void dumpAllAsLst() {
        for (Map.Entry<File, List<Integer>> fileEntry : coverage.entrySet()) {
            String absolutePath = fileEntry.getKey().getAbsolutePath();
            try (FileWriter writer = new FileWriter(absolutePath + ".lst")) {
                List<Integer> coverageInfo = fileEntry.getValue();

                int max = coverageInfo.stream().filter(Objects::nonNull).mapToInt(Integer::intValue).max().orElse(0);
                int len = String.valueOf(max).length();

                for (Integer integer : coverageInfo) {
                    writer.append(StringUtils.leftPad(integer != null ? integer.toString() : "", len, ' ')).append('|').append('\n');
                }

                writer.append(fileEntry.getKey().getName()).append(String.valueOf(' ')).append("is 100% covered");
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        coverage.clear();
    }
}
