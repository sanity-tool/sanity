package ru.urururu.sanity.cpp;

import junit.framework.Assert;
import junit.framework.ComparisonFailure;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import ru.urururu.sanity.cpp.tools.Language;
import ru.urururu.sanity.cpp.tools.Tool;
import ru.urururu.sanity.cpp.tools.ToolFactory;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
abstract class TestHelper {
    static ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("context.xml");

    static final String BASE = System.getProperty("TEST_RESOURCES_ROOT");
    private static String FAILURES_DIR = System.getProperty("TEST_FAILURES_ROOT");
    private static final BidiMap<Language, String> languageDirs = new DualHashBidiMap<>();

    private static ToolFactory toolFactory;

    static {
        context.refresh();

        toolFactory = context.getBean(ToolFactory.class);

        languageDirs.put(Language.C, "c");
        languageDirs.put(Language.Cpp, "cpp");
        languageDirs.put(Language.ObjectiveC, "o-c");
    }

    protected void fillWithTests(TestSuite suite, String path) {
        fillWithTests(suite, new File(BASE, path));
    }

    private void fillWithTests(TestSuite suite, File file) {
        for (final File f : file.listFiles()) {
            if (matches(f)) {
                suite.addTest(new TestCase(f.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        Tool testTool;
                        if (f.isDirectory()) {
                            String name = f.getName();
                            Language language = languageDirs.getKey(name);
                            testTool = toolFactory.get(language);
                        } else {
                            testTool = toolFactory.get(FilenameUtils.getExtension(f.getAbsolutePath()));
                        }

                        Path pathToExpected;
                        if (testTool == null) {
                            pathToExpected = Paths.get(f.getAbsolutePath() + ".expected.txt");
                        } else {
                            String versionId = testTool.getVersionId();

                            pathToExpected = Paths.get(f.getAbsolutePath() + '.' + versionId + ".expected.txt");
                            if (!pathToExpected.toFile().exists()) {
                                pathToExpected = Paths.get(f.getAbsolutePath() + ".expected.txt");
                            }
                        }

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

    protected boolean matches(File file) {
        return isSupportedByExtension(file);
    }

    private boolean isSupportedByExtension(File file) {
        return isExtensionSupported(FilenameUtils.getExtension(file.getName()));
    }

    private boolean isExtensionSupported(String extension) {
        return context.getBean(ToolFactory.class).getExtensions().contains(extension);
    }

    boolean isDirectorySupported(File file) {
        return file.isDirectory() && toolFactory.getLanguages().contains(languageDirs.getKey(file.getName()));
    }

    public abstract void runTest(String unit, Path pathToExpected) throws Exception;

    void check(Path pathToExpected, String actual) throws IOException, InterruptedException {
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
                Files.copy(pathToExpected, pathToExpected2, StandardCopyOption.REPLACE_EXISTING);

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
