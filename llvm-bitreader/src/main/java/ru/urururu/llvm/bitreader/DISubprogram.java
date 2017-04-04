package ru.urururu.llvm.bitreader;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class DISubprogram {
    private DIFile file;

    public DISubprogram(DIFile file) {
    }

    public void setFile(DIFile file) {
        if (file == null) {
            throw new IllegalArgumentException("file: " + file);
        }
        this.file = file;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getFilename() {
        return file != null ? file.getFilename() : null;
    }
}
