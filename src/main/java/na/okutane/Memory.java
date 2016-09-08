package na.okutane;

import na.okutane.api.cfg.ConstCache;
import na.okutane.api.cfg.GetElementPointer;
import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.Indirection;
import na.okutane.api.cfg.Parameter;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.TemporaryVar;
import na.okutane.api.cfg.Value;
import na.okutane.simulation.SimulationException;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class Memory implements Cloneable {
    Map<GlobalVariableCache.GlobalVar, Value> globalVars = new LinkedHashMap<>();
    Map<RValue, Value> stackVars = new LinkedHashMap<>();
    Map<Value, Value> heap = Collections.emptyMap();
    int unknownValues = 0;

    Memory putValue(RValue rValue, Value value) throws SimulationException {
        if (rValue instanceof Indirection) {
            Value pointer = getValue(((Indirection) rValue).getPointer());
            if (pointer instanceof ConstCache.NullPtr) {
                return null; // corruption
            }

            Memory result;
            try {
                result = (Memory) clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }

            result.heap = new LinkedHashMap<>(result.heap);
            result.heap.put(pointer, value);

            return result;
        }
        if (rValue instanceof TemporaryVar) {
            Memory result;
            try {
                result = (Memory) clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }

            result.stackVars = new LinkedHashMap<>(result.stackVars);
            result.stackVars.put(rValue, value);

            return result;
        }
        throw new IllegalStateException("Don't know how to put value for " + rValue.getClass().getSimpleName());
    }

    public Value getValue(RValue rValue) throws SimulationException {
        if (rValue instanceof Indirection) {
            Value pointer = getValue(((Indirection) rValue).getPointer());
            if (pointer instanceof ConstCache.NullPtr) {
                throw new SimulationException();
            }

            return heap.computeIfAbsent(pointer, unused -> new UnknownValue("U_" + unknownValues++));
        }
        if (rValue instanceof GetElementPointer) {
            Value pointer = getValue(((GetElementPointer) rValue).getPointer());
            if (pointer instanceof ConstCache.NullPtr) {
                return pointer; // todo not very correct, but better than non zero constant.
            }
        }
        if (rValue instanceof Value) {
            return (Value) rValue;
        }
        if (rValue instanceof GlobalVariableCache.GlobalVar) {
            return globalVars.computeIfAbsent((GlobalVariableCache.GlobalVar) rValue, globalVar -> new UnknownValue("G_" + globalVars.size()));
        }
        if (rValue instanceof Parameter || rValue instanceof TemporaryVar) {
            return stackVars.computeIfAbsent(rValue, unused -> new UnknownValue("U_" + unknownValues++));
        }
        throw new IllegalStateException("Don't know how to get value from " + rValue.getClass().getSimpleName());
    }

    public void dump(PrintStream stream) {
        if (globalVars.size() != 0) {
            stream.println("Globals:");
            for (Map.Entry<GlobalVariableCache.GlobalVar, Value> e : globalVars.entrySet()) {
                stream.println(e.getKey() + " -> " + e.getValue());
            }
        }
        if (stackVars.size() != 0) {
            stream.println("Locals:");
            for (Map.Entry<RValue, Value> e : stackVars.entrySet()) {
                stream.println(e.getKey() + " -> " + e.getValue());
            }
        }
        if (heap.size() != 0) {
            stream.println("Heap:");
            for (Map.Entry<Value, Value> e : heap.entrySet()) {
                stream.println(e.getKey() + " -> " + e.getValue());
            }
        }
    }

    private class UnknownValue implements Value {
        String id;

        public UnknownValue(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return id;
        }
    }
}