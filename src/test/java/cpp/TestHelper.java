package cpp;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;
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
    private static String BASE = System.getProperty("TEST_RESOURCES_ROOT");
    private static String FAILURES_DIR = System.getProperty("TEST_FAILURES_ROOT");

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

    protected void check(Path pathToExpected, String actual) throws IOException, InterruptedException {
        try {
            byte[] bytes = Files.readAllBytes(pathToExpected);
            String expected = new String(bytes, Charset.defaultCharset());

            Assert.assertEquals(expected, actual);
        } catch (ComparisonFailure e) {
            if (FAILURES_DIR != null) {
                Path failuresPath = Paths.get(FAILURES_DIR);

                Path expectedDir = failuresPath.resolve("expected");
                expectedDir.toFile().mkdirs();
                Path pathToExpected2 = expectedDir.resolve(pathToExpected.getFileName().toString());
                Files.copy(pathToExpected, pathToExpected2);

                Path actualDir = failuresPath.resolve("actual");
                actualDir.toFile().mkdirs();
                Path pathToActual = actualDir.resolve(pathToExpected.getFileName().toString());
                Files.write(pathToActual, actual.getBytes());
            }

            throw e;
        } catch (NoSuchFileException e) {
            Files.write(pathToExpected, actual.getBytes());
            Assert.fail("File " + pathToExpected + " not found, but I've created it for you anyways.");
        }
    }
}
