package na.okutane.utils;

import java.io.File;
import java.io.IOException;

/**
* @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
*/
public class TempFileWrapper implements AutoCloseable {
    File tempFile;

    public TempFileWrapper(String prefix, String suffix) throws IOException {
        this.tempFile = File.createTempFile(prefix, suffix);
    }

    @Override
    public void close() throws IOException {
        tempFile.delete();
    }

    public File getFile() {
        return tempFile;
    }

    public String getAbsolutePath() {
        return tempFile.getAbsolutePath();
    }
}
