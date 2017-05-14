package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.FlowAnalyzer;
import ru.urururu.sanity.MultiState;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.CfePrinter;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class FlowAnalyzerTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("simulator");

        new FlowAnalyzerTests().fillWithTests(suite, "simulator");

        return suite;
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        Parser parser = context.getBean(Parser.class);
        List<Cfg> testResult = parser.parse(unit, (prefix, suffix) -> getDebugPath(unit, prefix, suffix), true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Cfg cfg : testResult) {
            FlowAnalyzer analyzer = new FlowAnalyzer();
            Map<Cfe, MultiState> stateMap = analyzer.analyze(cfg);

            CfePrinter printer = new CfePrinter() {
                @Override
                public CfePrinter print0(Cfe cfe) {
                    super.print0(cfe);

                    printLine();

                    sb.append("Possible states:");
                    printLine();
                    sb.append(stateMap.get(cfe).get());

                    return this;
                }
            };

            ps.println("CFG: " + cfg.getId());
            printer.visitAll(CfgUtils.getAllCfes(cfg.getEntry()));
            ps.println(printer);
            ps.println();
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
