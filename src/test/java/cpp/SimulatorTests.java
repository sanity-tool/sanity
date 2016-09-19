package cpp;

import junit.framework.TestSuite;
import na.okutane.CallsMap;
import na.okutane.Simulator;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.Type;
import na.okutane.cpp.Parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
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

    void parseAll(Parser parser, File directory, List<Cfg> allCfgs) throws Exception {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                parseAll(parser, file, allCfgs);
            } else {
                allCfgs.addAll(parser.parse(file.getAbsolutePath()));
            }
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

        for (Cfg cfg : allCfgs) {
            if (!cfg.getId().contains("test")) {
                continue;
            }

            Simulator simulator = new Simulator(cfg, callsMap) {
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
                    ps.println(CfePrinter.print(cfe));
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
