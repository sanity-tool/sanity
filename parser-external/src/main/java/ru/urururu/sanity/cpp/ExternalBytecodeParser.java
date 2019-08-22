package ru.urururu.sanity.cpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.ConstCache;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ExternalBytecodeParser implements BytecodeParser {
    @Autowired
    private CfgUtils cfgUtils;

    @Autowired
    private ConstCache constants;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalBytecodeParser.class);


    @Override
    public List<Cfg> parse(File file) throws ParseException {
        try {
            File errFile = File.createTempFile("pre", "su");
            errFile.deleteOnExit();
            try {
                ProcessBuilder pb = new ProcessBuilder("/Users/dmitry.matveyev/okutane/cfgf/build/tools/NativeBytecodeParser/NativeBytecodeParser", file.getAbsolutePath());

                pb.inheritIO();
                pb.redirectError(ProcessBuilder.Redirect.to(errFile));

                Process process = pb.start();

                int resultCode = process.waitFor();

                if (resultCode == 0) {
                    return Collections.emptyList(); // todo parse em all
                } else {
                    String error = new String(Files.readAllBytes(Paths.get(errFile.getAbsolutePath())));
                    throw new ParseException(resultCode, error);
                }
            } catch (InterruptedException e) {
                throw new ParseException(e);
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
}
