package ru.urururu.sanity.cpp;

import com.codahale.metrics.Timer;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.ParserSettings;
import ru.urururu.sanity.cpp.tools.Language;
import ru.urururu.sanity.cpp.tools.Tool;
import ru.urururu.sanity.cpp.tools.ToolFactory;
import ru.urururu.sanity.cpp.util.Metrics;
import ru.urururu.sanity.utils.FileWrapper;
import ru.urururu.sanity.utils.TempFileWrapper;
import ru.urururu.util.Coverage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
abstract class TestHelper {
    static ApplicationContext context = new AnnotationConfigApplicationContext(TestsConfiguration.class);

    static final String BASE = System.getProperty("TEST_RESOURCES_ROOT");
    private static Path TESTS_PATH = Paths.get(BASE);
    private static String FAILURES_DIR = System.getProperty("TEST_FAILURES_ROOT");
    private static String DEBUG_DIR = System.getProperty("TEST_DEBUG_ROOT");
    private static final BidiMap<Language, String> languageDirs = new DualHashBidiMap<>();
    private static final String LANG = System.getProperty("TESTED_LANG");
    private static final String FILTER = System.getProperty("TEST_FILTER", "");
    private static final boolean UPDATE = Boolean.getBoolean("UPDATE_TESTS");

    private static ToolFactory toolFactory;

    static {
        toolFactory = context.getBean(ToolFactory.class);

        languageDirs.put(Language.C, "c");
        languageDirs.put(Language.Cpp, "cpp");
        languageDirs.put(Language.ObjectiveC, "o-c");

        Runtime.getRuntime().addShutdownHook(new Thread(Coverage::dumpAllAsLst));
    }

    void fillWithTests(TestSuite suite, String path) {
        try (Timer.Context ctx = Metrics.time(TestHelper.class, "fillWithTests")) {
            fillWithTests(suite, new File(BASE, path));
        }
    }

    private void fillWithTests(TestSuite suite, File file) {
        File[] files = file.listFiles();

        if (files == null) {
            throw new IllegalStateException("No files in " + file);
        }

        for (final File f : files) {
            if (matches(f)) {
                Tool testTool;
                if (f.isDirectory()) {
                    String name = f.getName();
                    Language language = languageDirs.getKey(name);
                    testTool = toolFactory.get(language);
                } else {
                    testTool = toolFactory.get(FilenameUtils.getExtension(f.getAbsolutePath()));
                }

                String absolutePath = f.getAbsolutePath();
                Path pathToExpected = getPathToExpected(f, testTool);

                suite.addTest(new TestCase(f.getName()) {
                    @Override
                    protected void runTest() throws Throwable {
                        try (Timer.Context ctx = Metrics.time(TestHelper.this.getClass(), "runTest")) {
                            TestHelper.this.runTest(absolutePath, pathToExpected);
                        }
                    }
                });
            } else if (f.isDirectory()) {
                TestSuite inner = new TestSuite(f.getName());
                fillWithTests(inner, f);
                suite.addTest(inner);
            }
        }
    }

    private Path getPathToExpected(File testFile, Tool tool) {
        if (tool != null) {
            List<String> versionIds = tool.getVersionIds();

            for (String versionId : versionIds) {
                Path pathToExpected = Paths.get(testFile.getAbsolutePath() + '.' + versionId + ".expected.txt");
                if (pathToExpected.toFile().exists()) {
                    return pathToExpected;
                }
            }
        }
        return Paths.get(testFile.getAbsolutePath() + ".expected.txt");
    }

    protected boolean matches(File file) {
        return file.getName().contains(FILTER) && isSupportedByExtension(file);
    }

    private boolean isSupportedByExtension(File file) {
        return isExtensionSupported(FilenameUtils.getExtension(file.getName()));
    }

    private boolean isExtensionSupported(String extension) {
        return isLanguageSupported(Language.getByExtension(extension));
    }

    boolean isDirectorySupported(File file) {
        return file.isDirectory() && isLanguageSupported(languageDirs.getKey(file.getName()));
    }

    private boolean isLanguageSupported(Language language) {
        if (language == null) {
            return false;
        }
        if (StringUtils.isNotEmpty(LANG)) {
            return language.toString().equals(LANG);
        }
        return toolFactory.getLanguages().contains(language);
    }

    public abstract void runTest(String unit, Path pathToExpected) throws Exception;

    void check(Path pathToExpected, String actual) throws IOException {
        try (Timer.Context ctx = Metrics.time(getClass(), "check")) {
            try {
                byte[] bytes = Files.readAllBytes(pathToExpected);
                String expected = new String(bytes, Charset.defaultCharset());

                Assert.assertEquals(expected, actual);
            } catch (ComparisonFailure e) {
                if (UPDATE) {
                    Files.write(pathToExpected, actual.getBytes());
                } else if (FAILURES_DIR != null) {
                    Path resultSubPath = TESTS_PATH.relativize(pathToExpected);
                    Path failuresPath = Paths.get(FAILURES_DIR);

                    Path expectedDir = failuresPath.resolve("expected");
                    Path pathToExpected2 = expectedDir.resolve(resultSubPath);
                    pathToExpected2.getParent().toFile().mkdirs();
                    Files.copy(pathToExpected, pathToExpected2, StandardCopyOption.REPLACE_EXISTING);

                    Path actualDir = failuresPath.resolve("actual");
                    Path pathToActual = actualDir.resolve(resultSubPath);
                    pathToActual.getParent().toFile().mkdirs();
                    Files.write(pathToActual, actual.getBytes());
                }

                throw e;
            } catch (NoSuchFileException e) {
                Files.write(pathToExpected, actual.getBytes());
                Assert.fail("File " + pathToExpected + " not found, but I've created it for you anyways.");
            }
        }
    }

    FileWrapper getDebugPath(String unit, String prefix, String suffix) {
        if (DEBUG_DIR != null) {
            Path debugPath = Paths.get(DEBUG_DIR);

            Path basePath = Paths.get(BASE);

            Path unitPath = Paths.get(unit);

            Path unitDir = debugPath.resolve(basePath.relativize(unitPath));
            File unitDirFile = unitDir.toFile();
            unitDirFile.mkdirs();

            return new FileWrapper(unitDir.resolve(prefix + suffix).toFile()) {
                @Override
                public void close() throws IOException {
                }
            };
        }

        return new TempFileWrapper(prefix, suffix);
    }

    List<Cfg> parseAll(Parser parser, File directory, Language language) throws Exception {
        List<Cfg> allCfgs = new ArrayList<>();

        File[] files = directory.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                allCfgs.addAll(parseAll(parser, file, language));
            } else if (language.getExtensions().contains(FilenameUtils.getExtension(file.getName()))) {
                allCfgs.addAll(parser.parse(file.getAbsolutePath(), (prefix, suffix) -> getDebugPath(file.getAbsolutePath(), prefix, suffix), true));
            }
        }

        return allCfgs;
    }

    Language getDirectoryLanguage(File directory) {
        Language result = languageDirs.getKey(directory.getName());

        if (result == null) {
            throw new IllegalArgumentException("Can't find language for " + directory);
        }

        return result;
    }

    @ComponentScan("ru.urururu.sanity")
    @Configuration
    public static class TestsConfiguration {
        @Bean
        @Primary
        ParserSettings parserSettings() {
            return new ParserSettings() {
                @Override
                public String maskLocal(String localName) {
                    switch (localName) {
                        case "exn.slot":
                        case "ehselector.slot":
                            // since clang 4 special locals like "exn.slot" and "ehselector.slot" are losing their names.
                            return "";
                    }

                    if (localName.endsWith(".addr")) {
                        // older clang (e.g. 3.3) name some variables with that suffix
                        return localName.substring(0, localName.length() - ".addr".length());
                    }

                    return super.maskLocal(localName);
                }
            };
        }
    }
}
