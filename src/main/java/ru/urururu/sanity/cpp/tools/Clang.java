package ru.urururu.sanity.cpp.tools;

import org.apache.commons.lang3.SystemUtils;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
class Clang extends Tool {
    Clang(String executable, String version) {
        super(executable, version);
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

    @Override
    String evaluateVersionId(String version) {
        String clangVersion = "clang version";
        if (version.startsWith(clangVersion)) {
            version = version.substring(clangVersion.length(), version.indexOf('(')).trim().replace(".", "");
            return "clang" + version;
        } else if (version.startsWith("Apple LLVM version")) {
            if (version.startsWith("Apple LLVM version 7.3.0")) {
                return "allvm73";
            }
        }

        return super.evaluateVersionId(version);
    }
}
