package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.Violation;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.rules.DivisionByZero;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

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
        List<Cfg> cfgs = parser.parse(unit);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        DivisionByZero rule = new DivisionByZero();

        for (Cfg cfg : cfgs) {
            rule.findAll(cfg, new Consumer<Violation>() {
                @Override
                public void accept(Violation violation) {
                    ps.println("Violation: " + violation.getValue());
                    Cfe cfe = violation.getPoint();
                    ps.println(cfe.getSourceRange());
                    ps.println();
                }
            });
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
