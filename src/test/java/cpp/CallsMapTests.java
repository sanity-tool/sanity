package cpp;

import junit.framework.TestSuite;
import na.okutane.CallsMap;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.api.cfg.Type;
import na.okutane.cpp.Parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
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

    void parseAll(Parser parser, File directory, List<Cfg> allCfgs) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                parseAll(parser, file, allCfgs);
            } else {
                allCfgs.addAll(parser.parse(file.getAbsolutePath()));
            }
        }
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        File directory = new File(unit);
        Parser parser = context.getBean(Parser.class);
        List<Cfg> allCfgs = new ArrayList<>();
        parseAll(parser, directory, allCfgs);

        CallsMap callsMap = context.getBean(CallsMap.class);
        callsMap.init(allCfgs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Map.Entry<String, List<Cfe>> entry : callsMap.getStaticCalls().entrySet()) {
            ps.println("CFG: " + entry.getKey());
            for (Cfe cfe : entry.getValue()) {
                ps.println(CfePrinter.print(cfe));
            }
            ps.println();
        }

        for (Map.Entry<Type, List<Cfe>> entry : callsMap.getCompatibleCalls().entrySet()) {
            ps.println("Type: " + entry.getKey());
            for (Cfe cfe : entry.getValue()) {
                ps.println(CfePrinter.print(cfe));
            }
            ps.println();
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
