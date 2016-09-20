package ru.urururu.sanity.cpp.tools;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Swift extends Tool {
    private String executable;

    Swift(String executable) {
        this.executable = executable;
    }

    @Override
    Set<Language> getLanguages() {
        return EnumSet.of(Language.Swift);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        return new String[]{executable, "-emit-bc", "-g", "-o", objFile, "-module-name", new File(filename).getName(), filename};
    }
}
