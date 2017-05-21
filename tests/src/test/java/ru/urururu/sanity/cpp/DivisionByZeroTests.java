package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class DivisionByZeroTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("rules");

        new DivisionByZeroTests().fillWithTests(suite, "rules/DBZ");

        return suite;
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        Parser parser = context.getBean(Parser.class);
        List<Cfg> allCfgs = new ArrayList<>();
        parseAll(parser, directory, allCfgs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        DivisionByZero rule = new DivisionByZero() {
            @Override
            protected void reportViolation(String rValue, Collection<Cfe> path) {
                ps.println("Violation: " + rValue);
                for (Cfe cfe : path) {
                    ps.println(cfe.getSourceRange());
                }
                ps.println();
            }
        };
    }
}
