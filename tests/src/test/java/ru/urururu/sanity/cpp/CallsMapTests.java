package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.CallsMap;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.sanity.cpp.tools.Language;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CallsMapTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("calls map");

        new CallsMapTests().fillWithTests(suite, "callsmap");

        return suite;
    }

    @Override
    protected boolean matches(File file) {
        return isDirectorySupported(file);
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        File directory = new File(unit);
        Parser parser = context.getBean(Parser.class);
        List<Cfg> allCfgs = parseAll(parser, directory, getDirectoryLanguage(directory));

        CallsMap callsMap = context.getBean(CallsMap.class);
        callsMap.init(allCfgs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Map.Entry<String, List<Cfe>> entry : callsMap.getStaticCalls().entrySet()) {
            ps.println("CFG: " + entry.getKey());
            for (Cfe cfe : entry.getValue()) {
                ps.println(cfe);
            }
            ps.println();
        }

        for (Map.Entry<Type, List<Cfe>> entry : callsMap.getCompatibleCalls().entrySet()) {
            ps.println("Type: " + entry.getKey());
            for (Cfe cfe : entry.getValue()) {
                ps.println(cfe);
            }
            ps.println();
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
