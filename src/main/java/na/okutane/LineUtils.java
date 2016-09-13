package na.okutane;

import javafx.util.Pair;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class LineUtils implements DisposableBean {
    private static final Map<Pair<File, Integer>, String> CACHE = new HashMap<>();

    public static String dumpLine(File file, int lineNumber) {
        return CACHE.computeIfAbsent(new Pair<>(file, lineNumber), key -> {
            System.out.println("file = [" + file + "], lineNumber = [" + lineNumber + "]");
            try {
                return Files.readAllLines(Paths.get(file.getAbsolutePath())).get(lineNumber - 1).trim();

//                LineNumberReader reader = new LineNumberReader(new FileReader(file));
//                String line;
//                do {
//                    line = reader.readLine();
//                } while (reader.getLineNumber() < lineNumber);
//                return line.trim();
            } catch (Throwable e) {
                // todo fix
                return null;
            }
        });
    }

    @Override
    public void destroy() throws Exception {
        CACHE.clear();
    }
}
