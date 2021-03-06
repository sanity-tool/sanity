package ru.urururu.sanity.cpp;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.cpp.tools.Tool;
import ru.urururu.sanity.cpp.tools.ToolFactory;
import ru.urururu.sanity.utils.FileWrapper;
import ru.urururu.sanity.utils.TempFileWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    @Autowired
    private ToolFactory tools;

    @Autowired
    BytecodeParser bytecodeParser;

    public List<Cfg> parse(String filename) throws ParseException {
        return parse(filename, TempFileWrapper::new, false);
    }

    public List<Cfg> parse(String filename, BiFunction<String, String, FileWrapper> fileWrapperFactory, boolean produceDebug) throws ParseException {
        LOGGER.info("filename = {}", filename);
        try {
            try (FileWrapper objFile = fileWrapperFactory.apply("result", ".bc")) {
                try (FileWrapper errFile = fileWrapperFactory.apply("result", ".err.log")) {
                    Tool tool = tools.get(FilenameUtils.getExtension(filename));

                    if (produceDebug) {
                        try (FileWrapper debugFile = fileWrapperFactory.apply("debug", ".ll")) {
                            try (FileWrapper debugErrFile = fileWrapperFactory.apply("debug", ".err.log")) {
                                String[] parameters = tool.createDebugParameters(filename, debugFile.getAbsolutePath());
                                if (parameters != null) {
                                    LOGGER.info("debugParameters = {}", Arrays.toString(parameters));

                                    ProcessBuilder pb = new ProcessBuilder(parameters);

                                    pb.inheritIO();
                                    pb.redirectError(ProcessBuilder.Redirect.to(debugErrFile.getFile()));

                                    Process process = pb.start();

                                    process.waitFor();
                                }
                            }
                        }
                    }

                    String[] parameters = tool.createParameters(filename, objFile.getAbsolutePath());
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
