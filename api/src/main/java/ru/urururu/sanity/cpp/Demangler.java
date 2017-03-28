package ru.urururu.sanity.cpp;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Demangler {
    public static String demangle(String mangled) {
        String prefix = "_ZN";
        if (!mangled.startsWith(prefix)) {
            return mangled;
        }

        char[] chars = mangled.toCharArray();

        int pos = prefix.length();

        List<String> parts = new ArrayList<>();

        while (pos < chars.length) {
            int count = 0;
            while (Character.isDigit(chars[pos + count])) {
                count++;
            }

            if (count == 0) {
                if (chars[pos] == 'E') {
                    // end of arguments list
                    pos++;
                    break;
                }

                // todo implement
                parts.add(new String(chars, pos, chars.length - pos));
                break;
            }

            String len = new String(chars, pos, count);
            int length = Integer.parseInt(len);
            parts.add(new String(chars, pos + count, length));

            pos += count + length;
        }

        return parts.stream().collect(Collectors.joining("::"));
    }
}
