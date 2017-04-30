package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.LineUtils;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SourceRange {
    private final String filename;
    private final long line;

    public SourceRange(String filename, long line) {
        this.filename = filename;
        this.line = line;
    }

    public String getFilename() {
        return filename;
    }

    public long getLine() {
        return line;
    }

    @Override
    public String toString() {
        File sourceFile = new File(filename);
        return sourceFile.getName() + ':' + line + ' ' + LineUtils.dumpLine(sourceFile, line);
    }
}
