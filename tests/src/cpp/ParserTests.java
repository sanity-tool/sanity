package cpp;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import na.okutane.cpp.Parser;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
public class ParserTests {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite();

        suite.addTest(new TestCase() {
            @Override
            public void runTest() {
                new Parser().parse("res/rules/NP/basic.c");
            }
        });

        return suite;
    }
}
