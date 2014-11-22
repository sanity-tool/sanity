package cpp;

import junit.framework.Assert;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public abstract class TestHelper {
    protected static ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");
    static {
        context.refresh();
    }
    private static String BASE = "/Users/jondoe/IdeaProjects/SA/sanity/tests/res";

    protected void fillWithTests(TestSuite suite, String path) {
        fillWithTests(suite, new File(BASE, path));
    }

    protected void fillWithTests(TestSuite suite, File file) {
        for (final File f : file.listFiles()) {
            if (matches(f)) {
                suite.addTest(new TestCase(f.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        Path pathToExpected = Paths.get(f.getAbsolutePath() + ".expected.txt");
                        TestHelper.this.runTest(f.getAbsolutePath(), pathToExpected);
                    }
                });
            } else if (f.isDirectory()) {
                TestSuite inner = new TestSuite(f.getName());
                fillWithTests(inner, f);
                suite.addTest(inner);
            }
        }
    }

    protected abstract boolean matches(File file);

    public abstract void runTest(String unit, Path pathToExpected) throws Exception;

    protected void check(Path pathToExpected, String actual) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(pathToExpected);
            String expected = new String(bytes, Charset.defaultCharset());

            Assert.assertEquals(expected, actual);
        } catch (NoSuchFileException e) {
            Files.write(pathToExpected, actual.getBytes());
            Assert.fail("File " + pathToExpected + " not found, but I've created it for you anyways.");
        }
    }
}
