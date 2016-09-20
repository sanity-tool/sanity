package na.okutane.api.cfg;

import na.okutane.LineUtils;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SourceRange {
    private final String filename;
    private final int line;

    public SourceRange(String filename, int line) {
        this.filename = filename;
        this.line = line;
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        File sourceFile = new File(filename);
        return sourceFile.getName() + ':' + line + ' ' + LineUtils.dumpLine(sourceFile, line);
    }
}
