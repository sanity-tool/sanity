package ru.urururu.sanity.cpp.tools;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class LlvmAs extends Tool {
    private final String executable;

    private LlvmAs(String executable) {
        this.executable = executable;
    }

    static Optional<Tool> tryCreate(String executable) throws InterruptedException {
        return tryCreate(executable, LlvmAs::new);
    }

    @Override
    Set<Language> getLanguages() {
        return EnumSet.of(Language.IR);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        return new String[]{executable, "-o=" + objFile, filename};
    }
}
