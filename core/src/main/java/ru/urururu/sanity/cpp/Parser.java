package ru.urururu.sanity.cpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.utils.TempFileWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    @Autowired
    ClangParametersFactory parametersFactory;

    @Autowired
    BytecodeParser bytecodeParser;

    public List<Cfg> parse(String filename) throws ParseException {
        LOGGER.info("filename = {}", filename);
        try {
            try (TempFileWrapper objFile = new TempFileWrapper("result", ".bc")) {
                try (TempFileWrapper errFile = new TempFileWrapper("result", ".err")) {
                    String[] parameters = parametersFactory.getParameters(filename, objFile.getAbsolutePath());
                    LOGGER.info("parameters = {}", Arrays.toString(parameters));

                    ProcessBuilder pb = new ProcessBuilder(parameters);

                    pb.inheritIO();
                    pb.redirectError(ProcessBuilder.Redirect.to(errFile.getFile()));

                    Process process = pb.start();

                    int resultCode = process.waitFor();

                    if (resultCode == 0) {
                        return bytecodeParser.parse(objFile.getFile());
                    } else {
                        String error = new String(Files.readAllBytes(Paths.get(errFile.getAbsolutePath())));
                        throw new ParseException(resultCode, error);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new ParseException(e);
        }
    }
}