package ru.urururu.sanity.rules;

import ru.urururu.sanity.CallsMap;
import ru.urururu.sanity.Simulator;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Assignment;
import ru.urururu.sanity.api.cfg.Call;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.CfePrinter;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.GetElementPointer;
import ru.urururu.sanity.api.cfg.IfCondition;
import ru.urururu.sanity.api.cfg.Indirection;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.Switch;
import ru.urururu.sanity.simulation.SimulationException;

import java.util.Collection;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
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
