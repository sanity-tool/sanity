package na.okutane;

import na.okutane.api.Cfg;
import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.api.cfg.CfeVisitor;
import na.okutane.api.cfg.IfCondition;
import na.okutane.api.cfg.NoOp;
import na.okutane.api.cfg.Switch;
import na.okutane.api.cfg.UnprocessedElement;

import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
public class Simulator {
    List<MachineState> states = new ArrayList<>();
    CallsMap callsMap;

    public Simulator(Cfg cfg, CallsMap callsMap) {
        states.add(new MachineState(cfg.getEntry()));
        this.callsMap = callsMap;
    }

    public boolean hasUnfinished() {
        return !states.isEmpty();
    }

    public void advanceAll() {
        List<MachineState> newStates = new ArrayList<>(states.size());
        for (MachineState state : states) {
            newStates.addAll(state.advance());
        }
        states = newStates;
    }

    protected class MachineState implements CfeVisitor {
        final Deque<Cfe> path;
        List<MachineState> paths = new ArrayList<>();

        public MachineState(Cfe position) {
            path = new ArrayDeque<>();
            path.add(position);
        }

        public MachineState(Deque<Cfe> path, Cfe position) {
            this.path = new ArrayDeque<>(path);
            path.add(position);
        }

        public Cfe getPosition() {
            return path.getLast();
        }

        public List<MachineState> advance() {
            getPosition().accept(this);
            return paths;
        }

        public void visitSimple(Cfe element) {
            Cfe next = element.getNext();
            if (next != null) {
                path.add(element.getNext());
                paths = Collections.singletonList(this);
            } else {
                // todo check for return point?
                paths = Collections.emptyList();
            }
        }

        @Override
        public void visit(UnprocessedElement element) {
            visitSimple(element);
        }

        @Override
        public void visit(Assignment assignment) {
            visitSimple(assignment);
        }

        @Override
        public void visit(Call call) {
            visitSimple(call);
            onMethodCall(call, this);
        }

        @Override
        public void visit(IfCondition ifCondition) {
            paths = Arrays.asList(
                    new MachineState(path, ifCondition.getThenElement()),
                    new MachineState(path, ifCondition.getThenElement())
            );
        }

        @Override
        public void visit(Switch switchElement) {
            paths = new ArrayList<>(switchElement.getCases().size() + 1);
            paths.add(new MachineState(path, switchElement.getDefaultCase()));
            for (Cfe e : switchElement.getCases().values()) {
                paths.add(new MachineState(path, e));
            }
        }

        @Override
        public void visit(NoOp noOp) {
            visitSimple(noOp);
        }

        public void dump(PrintStream stream) {
            for (Cfe cfe : path) {
                stream.append(CfePrinter.print(cfe));
            }
            stream.append("\n");
        }
    }

    protected void onMethodCall(Call call, MachineState state) {

    }
}
