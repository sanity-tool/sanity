package na.okutane;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class LineUtils {
    public static String dumpLine(File file, int lineNumber) {
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
    }
}
