package cpp;

import junit.framework.TestSuite;
import na.okutane.CallsMap;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.cpp.Parser;
import na.okutane.rules.NullPointer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class RuleTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("rules");

        new RuleTests().fillWithTests(suite, "rules/NP");

        return suite;
    }

    @Override
    protected boolean matches(File file) {
        return file.getName().endsWith(".c") || file.getName().endsWith(".cpp") || file.getName().endsWith(".m") || file.getName().endsWith(".ll") || file.getName().endsWith(".swift");
    }

    void parseAll(Parser parser, File file, List<Cfg> allCfgs) throws Exception {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                parseAll(parser, child, allCfgs);
            }
        } else {
            allCfgs.addAll(parser.parse(file.getAbsolutePath()));
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

        NullPointer rule = new NullPointer() {
            @Override
            protected  void reportViolation(String rValue, Collection<Cfe> path) {
                ps.println("Violation: " + rValue);
                for (Cfe cfe : path) {
                    ps.println(cfe.getSourceRange());
                }
                ps.println();
            }

            @Override
            protected void onError(Cfe cfe, Throwable e) {
                ps.println(CfePrinter.print(cfe));
                ps.println(e);
                ps.println();
            }
        };

        for (Cfg cfg : allCfgs) {
            rule.enforce(cfg, callsMap);
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
