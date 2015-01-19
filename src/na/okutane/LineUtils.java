package na.okutane;

import javafx.util.Pair;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class LineUtils implements DisposableBean {
    private static final Map<Pair<File, Integer>, String> CACHE = new HashMap<>();

    public static String dumpLine(File file, int lineNumber) {
        return CACHE.computeIfAbsent(new Pair(file, lineNumber), key -> {
            try {
                LineNumberReader reader = new LineNumberReader(new FileReader(file));
                String line;
                do {
                    line = reader.readLine();
                } while (reader.getLineNumber() < lineNumber);
                return line.trim();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        CACHE.clear();
    }
}
