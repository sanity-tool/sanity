package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.rules.NullPointer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RuleTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("rules");

        new RuleTests().fillWithTests(suite, "rules/NP");

        suite.addTest(DivisionByZeroTests.suite());

        return suite;
    }

    private void parseAll(Parser parser, File file, List<Cfg> allCfgs) throws Exception {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                parseAll(parser, child, allCfgs);
            }
        } else {
            allCfgs.addAll(parser.parse(file.getAbsolutePath(), (prefix, suffix) -> getDebugPath(file.getAbsolutePath(), prefix, suffix), true));
        }
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        File directory = new File(unit);
        Parser parser = context.getBean(Parser.class);
        List<Cfg> allCfgs = new ArrayList<>();
        parseAll(parser, directory, allCfgs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        NullPointer rule = new NullPointer() {
            @Override
            protected void reportViolation(String rValue, Collection<Cfe> path) {
                ps.println("Violation: " + rValue);
                for (Cfe cfe : path) {
                    ps.println(cfe.getSourceRange());
                }
                ps.println();
            }

            @Override
            protected void onError(Cfe cfe, Throwable e) {
                ps.println(cfe);
                ps.println(e);
                ps.println();
            }
        };

        for (Cfg cfg : allCfgs) {
            rule.enforce(cfg);
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
