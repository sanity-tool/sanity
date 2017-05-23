package ru.urururu.sanity.cpp;

import junit.framework.TestSuite;
import ru.urururu.sanity.CallsMap;
import ru.urururu.sanity.Simulator;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Call;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.RValue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class SimulatorTests extends TestHelper {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite("simulator");

        new SimulatorTests().fillWithTests(suite, "simulator");

        return suite;
    }

    @Override
    protected boolean matches(File file) {
        return isDirectorySupported(file);
    }

    @Override
    public void runTest(String unit, Path pathToExpected) throws Exception {
        File directory = new File(unit);
        Parser parser = context.getBean(Parser.class);
        List<Cfg> allCfgs = parseAll(parser, directory, getDirectoryLanguage(directory));

        CallsMap callsMap = context.getBean(CallsMap.class);
        callsMap.init(allCfgs);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream ps = new PrintStream(baos);

        for (Cfg cfg : allCfgs) {
            if (!cfg.getId().contains("test")) {
                continue;
            }

            Simulator simulator = new Simulator(cfg) {
                @Override
                protected MachineState createState() {
                    return new MachineState() {
                        @Override
                        public void visit(Call call) {
                            RValue function = call.getFunction();
                            if (function.toString().equals("@dump")) {
                                dump(ps);
                                ps.println();
                            }
                            super.visit(call);
                        }
                    };
                }

                @Override
                protected void onError(Cfe cfe, Throwable e) {
                    ps.println(cfe);
                    ps.println(e);
                    ps.println();
                }
            };

            while (simulator.hasUnfinished()) {
                simulator.advanceAll();
            }
        }

        String actual = baos.toString();
        check(pathToExpected, actual);
    }
}
