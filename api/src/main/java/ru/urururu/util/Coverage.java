package ru.urururu.util;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Coverage {
    private static final Map<File, List<Integer>> coverage = new HashMap<>();

    public synchronized static void markAsCode(File file, int line) {
        List<Integer> coverageInfo = coverage.computeIfAbsent(file, __ -> new ArrayList<>());
        while (coverageInfo.size() < line + 1) {
            coverageInfo.add(null);
        }

        Integer oldInfo = coverageInfo.get(line);
        coverageInfo.set(line, oldInfo == null ? 0 : oldInfo);
    }

    public synchronized static void hit(File file, int line) {
        List<Integer> coverageInfo = coverage.get(file);
        coverageInfo.set(line, coverageInfo.get(line) + 1);
    }

    public synchronized static void dumpAllAsLst() {
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
                e.printStackTrace();
            }
        }

        coverage.clear();
    }
}
