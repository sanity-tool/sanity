package ru.urururu.sanity.cpp.tools;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class Tool {
    abstract Set<Language> getLanguages();

    protected static Optional<Tool> tryCreate(String executable, Function<String, Tool> factory) throws InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(executable, "--version");
        try {
            pb.start();
            int result = pb.start().waitFor();
        } catch (IOException e) {
            return Optional.empty();
        }

        return Optional.of(factory.apply(executable));

    }

    public abstract String[] createParameters(String filename, String objFile);
}
