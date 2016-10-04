package ru.urururu.sanity.cpp.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class Tool {
    protected final String executable;
    private final String version;
    private final String versionId;

    Tool(String executable, String version) {
        this.executable = executable;
        this.version = version;
        this.versionId = evaluateVersionId(version);
        System.out.println("versionId = " + versionId);
    }

    abstract Set<Language> getLanguages();

    protected static Optional<Tool> tryCreate(String executable, BiFunction<String, String, Tool> factory) throws InterruptedException {
        String version;

        ProcessBuilder pb = new ProcessBuilder(executable, "--version");
        try {
            Process process = pb.start();

            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(process.getInputStream()))){
                version = reader.readLine();
            }

            pb.start().waitFor();
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(factory.apply(executable, version));
    }

    public abstract String[] createParameters(String filename, String objFile);

    public String getVersionId() {
        return versionId;
    }

    String evaluateVersionId(String version) {
        System.err.println("unknown version = " + version);
        return "unknown";
    }
}
