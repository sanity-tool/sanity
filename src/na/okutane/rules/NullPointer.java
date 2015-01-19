package na.okutane.rules;

import na.okutane.CallsMap;
import na.okutane.Simulator;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.api.cfg.ConstCache;
import na.okutane.api.cfg.GetElementPointer;
import na.okutane.api.cfg.IfCondition;
import na.okutane.api.cfg.Indirection;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.Switch;
import na.okutane.simulation.SimulationException;

import java.util.Collection;
import java.util.Deque;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class NullPointer {
    public void enforce(Cfg cfg, CallsMap callsMap) {
        Simulator simulator = new Simulator(cfg, callsMap) {
            @Override
            protected MachineState createState() {
                return new MachineState() {
                    @Override
                    public void visit(Assignment assignment) {
                        checkIndirection(assignment.getLeft());
                        checkIndirection(assignment.getRight());
                        super.visit(assignment);
                    }

                    @Override
                    public void visit(IfCondition ifCondition) {
                        checkIndirection(ifCondition.getCondition());
                        super.visit(ifCondition);
                    }

                    @Override
                    public void visit(Switch switchElement) {
                        if (checkIndirection(switchElement.getControl())) {
                            return;
                        }
                        super.visit(switchElement);
                    }

                    @Override
                    public void visit(Call call) {
                        checkIndirection(call.getFunction());
                        for (RValue arg : call.getArgs()) {
                            checkIndirection(arg);
                        }
                        checkIndirection(call.getlValue());
                        super.visit(call);
                    }

                    private boolean checkIndirection(RValue rValue) {
                        if (rValue instanceof Indirection) {
                            RValue pointer = ((Indirection) rValue).getPointer();
                            if (checkIndirection(pointer) || checkForNull(pointer)) {
                                return true;
                            }
                        }
                        if (rValue instanceof GetElementPointer) {
                            RValue pointer = ((GetElementPointer) rValue).getPointer();
                            RValue index = ((GetElementPointer) rValue).getIndex();
                            return checkIndirection(pointer) || checkIndirection(index);
                        }
                        return false;
                    }

                    private boolean checkForNull(RValue pointer) {
                        try {
                            if (getMemory().getValue(pointer) instanceof ConstCache.NullPtr) {
                                reportViolation(CfePrinter.printValue(pointer) + " is null", getPath());
                                return true;
                            }
                        } catch (SimulationException e) {
                            throw new IllegalStateException(e);
                        }
                        return false;
                    }
                };
            }

            @Override
            protected void onError(Cfe cfe, Throwable e) {
                NullPointer.this.onError(cfe, e);
            }
        };

        while (simulator.hasUnfinished()) {
            simulator.advanceAll();
        }
    }

    protected void onError(Cfe cfe, Throwable e) {
    }

    protected void reportViolation(String rValue, Collection<Cfe> path) {
    }
}
