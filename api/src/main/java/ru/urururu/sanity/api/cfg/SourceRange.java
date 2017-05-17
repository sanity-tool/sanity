package ru.urururu.sanity.api.cfg;

import ru.urururu.sanity.LineUtils;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SourceRange {
    private final File file;
    private final int line;

    public SourceRange(File file, int line) {
        this.file = file;
        this.line = line;
    }

    public File getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return file.getName() + ':' + line + ' ' + LineUtils.dumpLine(file, line);
    }
}
