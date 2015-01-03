package na.okutane;

import na.okutane.api.Cfg;
import na.okutane.api.cfg.Assignment;
import na.okutane.api.cfg.Call;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfePrinter;
import na.okutane.api.cfg.CfeVisitor;
import na.okutane.api.cfg.IfCondition;
import na.okutane.api.cfg.NoOp;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.Switch;
import na.okutane.api.cfg.UnprocessedElement;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

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
        Memory memory;
        List<MachineState> paths;

        public MachineState(Cfe position) {
            path = new ArrayDeque<>();
            memory = new Memory();
            path.add(position);
        }

        public MachineState(Deque<Cfe> path, Cfe position, Memory memory) {
            this.path = new ArrayDeque<>(path);
            this.path.add(position);
            this.memory = memory;
        }

        public Cfe getPosition() {
            return path.getLast();
        }

        public List<MachineState> advance() {
            Cfe position = getPosition();
            try {
                position.accept(this);
                return paths;
            } catch (Throwable e) {
                onError(position, e);
                return Collections.emptyList();
            }
        }

        public void visitSimple(Cfe element) {
            if (memory == null) {
                // memory has been corrupted.
                paths = Collections.emptyList();
                return;
            }
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
            memory = memory.putValue(assignment.getLeft(), memory.getValue(assignment.getRight()));
            visitSimple(assignment);
        }

        @Override
        public void visit(Call call) {
            visitSimple(call);
            onMethodCall(call, this);
        }

        @Override
        public void visit(IfCondition ifCondition) {
            // todo memory.getValue to check if branch known
            paths = Arrays.asList(
                    new MachineState(path, ifCondition.getThenElement(), memory), // todo memory.putValue(ifCondition.getControlValue(), true)
                    new MachineState(path, ifCondition.getElseElement(), memory)  // todo memory.putValue(ifCondition.getControlValue(), false)
            );
        }

        @Override
        public void visit(Switch switchElement) {
            // todo memory.getValue to check if case known
            paths = new ArrayList<>(switchElement.getCases().size() + 1);
            paths.add(new MachineState(path, switchElement.getDefaultCase(), memory)); // todo memory.putValue(switchElement.getControlValue(), not(switchElement.getCases().keys()))
            for (Map.Entry<RValue, Cfe> e : switchElement.getCases().entrySet()) {
                paths.add(new MachineState(path, e.getValue(), memory)); // todo memory.putValue(switchElement.getControlValue(), e.getKey())
            }
        }

        @Override
        public void visit(NoOp noOp) {
            visitSimple(noOp);
        }

        public void dump(PrintStream stream) {
            memory.dump(stream);
            stream.println("Path:");
            stream.println(CfePrinter.printAll(path));
        }

        @Override
        public String toString() {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            dump(ps);
            return baos.toString();
        }
    }

    protected void onError(Cfe cfe, Throwable e) {

    }

    protected void onMethodCall(Call call, MachineState state) {

    }
}
