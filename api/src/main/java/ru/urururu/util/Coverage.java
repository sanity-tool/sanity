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
    private static Map<File, List<Integer>> coverage;

    public static void markAsCode(String filename, int line) {
        line--;
        ensureMapCreated();
        List<Integer> coverageInfo = coverage.computeIfAbsent(new File(filename), __ -> new ArrayList<>());
        while (coverageInfo.size() < line + 1) {
            coverageInfo.add(null);
        }

        Integer oldInfo = coverageInfo.get(line);
        coverageInfo.set(line, oldInfo == null ? 0 : oldInfo);
    }

    private static void ensureMapCreated() {
        if (coverage == null) {
            coverage = new HashMap<>();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    for (Map.Entry<File, List<Integer>> fileEntry : coverage.entrySet()) {
                        String absolutePath = fileEntry.getKey().getAbsolutePath();
                        try (FileWriter writer = new FileWriter(absolutePath.substring(0, absolutePath.lastIndexOf('.')) + ".lst")) {
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
                }
            });
        }
    }

    public static void hit(File file, int line) {
        line--;
        List<Integer> coverageInfo = coverage.get(file);
        coverageInfo.set(line, coverageInfo.get(line) + 1);
    }
}
