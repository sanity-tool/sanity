package ru.urururu.sanity.cpp.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class Tool {
    private final static Logger LOGGER = LoggerFactory.getLogger(Tool.class);

    final String executable;
    private final List<String> versionIds;

    Tool(String executable, String version) {
        this.executable = executable;
        this.versionIds = evaluateVersionIds(version);

        LOGGER.info("executable = {}", executable);
        LOGGER.info("versionString = {}", version);
        LOGGER.info("versionIds = {}", versionIds);
    }

    abstract Set<Language> getLanguages();

    static Optional<Tool> tryCreate(String executable, BiFunction<String, String, Tool> factory) throws InterruptedException {
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

    /**
     * @return version identifiers from most specific to more generic
     */
    public List<String> getVersionIds() {
        return versionIds;
    }

    List<String> evaluateVersionIds(String version) {
        LOGGER.warn("unknown version = {}", version);
        return Collections.singletonList("unknown");
    }
}
