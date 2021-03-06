package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.CfePrinter;
import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.util.Coverage;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class ParserTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("parser");

        new ParserTests().fillWithTests(suite, "cfg");

        TestSuite parseErrors = new TestSuite("parse errors");
        new TestHelper() {
            @Override
            public void runTest(String unit, Path pathToExpected) throws Exception {
                Parser parser = context.getBean(Parser.class);
                try {
                    parser.parse(unit, (prefix, suffix) -> getDebugPath(unit, prefix, suffix), true);
                    throw new IllegalStateException("should have failed");
                } catch (ParseException e) {
                    check(pathToExpected, e.toString().replace(BASE, ""));
                }

            }
        }.fillWithTests(parseErrors, "errors/cfg");
        suite.addTest(parseErrors);

        return suite;
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        Parser parser = context.getBean(Parser.class);

        CfePrinter printer = new CfePrinter() {
            @Override
            protected String printSourceRange(SourceRange sourceRange) {
                Coverage.hit(sourceRange);
                return super.printSourceRange(sourceRange);
            }
        };

        List<Cfg> testResult = parser.parse(unit, (prefix, suffix) -> getDebugPath(unit, prefix, suffix), true);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Cfg cfg : testResult) {
            ps.println("CFG: " + cfg.getId());
            ps.println(printer.print(cfg));
            ps.println();
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
