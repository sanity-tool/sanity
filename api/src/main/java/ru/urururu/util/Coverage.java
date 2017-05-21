package ru.urururu.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Coverage {
    private static final Logger LOGGER = LoggerFactory.getLogger(Coverage.class);
    private static final Map<File, List<Integer>> coverage = new HashMap<>();

    private Coverage() {
        // prevent creation
    }

    public static synchronized void markAsCode(File file, int line) {
        List<Integer> coverageInfo = coverage.computeIfAbsent(file, __ -> new ArrayList<>());
        while (coverageInfo.size() < line + 1) {
            coverageInfo.add(null);
        }

        Integer oldInfo = coverageInfo.get(line);
        coverageInfo.set(line, oldInfo == null ? 0 : oldInfo);
    }

    public static synchronized void hit(File file, int line) {
        List<Integer> coverageInfo = coverage.get(file);

        if (coverageInfo.size() <= line) {
            LOGGER.warn("Line: " + line + ", Size: " + coverageInfo.size());
            LOGGER.info("Coverage: " + coverageInfo);
            return;//throw new IndexOutOfBoundsException("Line: " + line + ", Size: " + coverageInfo.size());
        }

        Integer integer = coverageInfo.get(line);

        if (integer == null) {
            LOGGER.warn("Line: " + line + ", Size: " + coverageInfo.size());
            LOGGER.info("Coverage: " + coverageInfo);
            return;//throw new IllegalStateException("Not marked as code. " + "Line: " + line + ", Size: " + coverageInfo.size());
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
