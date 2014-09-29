package cpp;

import junit.framework.TestSuite;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.cpp.Parser;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class ParserTests extends TestHelper {
    private static ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
    static {
        context.refresh();
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        new ParserTests().fillWithTests(suite, "cfg");

        return suite;
    }

    @Override
    protected boolean matches(File file) {
        return file.getName().endsWith(".c") || file.getName().endsWith(".cpp");
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        Parser parser = context.getBean(Parser.class);
        List<Cfg> testResult = parser.parse(unit);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Cfg cfg : testResult) {
            ps.println("CFG: " + cfg.getId());
            ps.println(CfePrinter.print(cfg));
            ps.println();
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
