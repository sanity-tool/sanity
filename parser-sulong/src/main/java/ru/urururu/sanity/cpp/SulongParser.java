package ru.urururu.sanity.cpp;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.parser.BitcodeParserResult;
import com.oracle.truffle.llvm.runtime.LLVMLanguage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongParser implements BytecodeParser {
    @Autowired
    SulongParsersFacade parsers;

    @Override
    public List<Cfg> parse(File file) {
        List<Cfg> result = new ArrayList<>();

        try {
            Source source = Source.newBuilder(file).mimeType(LLVMLanguage.LLVM_BITCODE_MIME_TYPE).build();
            BitcodeParserResult bitcodeParserResult = BitcodeParserResult.getFromSource(source);

            CfgBuilder moduleParser = new CfgBuilder(parsers, result);
            bitcodeParserResult.getModel().accept(moduleParser);

            Cfe entry = moduleParser.getInitFirst();
            if (entry != null) {
                result.add(new Cfg("<module init>", entry));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }
}
