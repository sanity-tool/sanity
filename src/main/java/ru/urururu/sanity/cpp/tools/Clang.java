package ru.urururu.sanity.cpp.tools;

import org.apache.commons.lang3.SystemUtils;

import java.util.*;

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
    List<String> evaluateVersionIds(String version) {
        String clangVersion = "clang version";
        if (version.startsWith(clangVersion)) {
            version = version.substring(clangVersion.length(), version.indexOf('(')).trim();
            return createVersionsFamily("clang", version);
        } else {
            String appleLlvmVersion = "Apple LLVM version";
            if (version.startsWith(appleLlvmVersion)) {
                version = version.substring(appleLlvmVersion.length(), version.indexOf('(')).trim();
                return createVersionsFamily("allvm", version);
            }
        }

        return super.evaluateVersionIds(version);
    }

    private List<String> createVersionsFamily(String prefix, String version) {
        String[] versionParts = version.split(".");

        String[] result = new String[versionParts.length];

        StringBuilder sb = new StringBuilder(prefix);
        for (int i = 0; i < versionParts.length; i++) {
            sb.append(versionParts[i]);
            result[result.length - i] = sb.toString();
        }

        return Collections.unmodifiableList(Arrays.asList(result));
    }
}
