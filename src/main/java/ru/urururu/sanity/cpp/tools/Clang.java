package ru.urururu.sanity.cpp.tools;

import org.apache.commons.lang3.SystemUtils;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class Clang extends Tool {
    private final String executable;

    private Clang(String executable) {
        this.executable = executable;
    }

    static Optional<Tool> tryCreate(String executable) throws InterruptedException {
        return tryCreate(executable, Clang::new);
    }

    @Override
    Set<Language> getLanguages() {
        return SystemUtils.IS_OS_MAC ? EnumSet.of(Language.C, Language.Cpp, Language.ObjectiveC) : EnumSet.of(Language.C, Language.Cpp);
    }

    @Override
    public String[] createParameters(String filename, String objFile) {
        if (filename.endsWith("hello.m")) {
            return new String[]{executable, "-framework", "Foundation", filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile};
        }

        return new String[]{executable, filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile};
    }
}
