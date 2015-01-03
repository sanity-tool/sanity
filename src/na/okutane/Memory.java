package na.okutane;

import na.okutane.api.cfg.ConstCache;
import na.okutane.api.cfg.GlobalVariableCache;
import na.okutane.api.cfg.Indirection;
import na.okutane.api.cfg.RValue;
import na.okutane.api.cfg.Value;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
public class Memory implements Cloneable {
    Map<GlobalVariableCache.GlobalVar, Value> globalVars = new LinkedHashMap<>();
    Map<Value, Value> heap = Collections.emptyMap();

    Memory putValue(RValue rValue, Value value) {
        if (rValue instanceof Indirection) {
            Value pointer = getValue(((Indirection) rValue).getPointer());
            if (pointer instanceof ConstCache.NullPtr) {
                return null;
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
        throw new IllegalStateException("Don't know how to put value for " + rValue.getClass().getSimpleName());
    }

    public Value getValue(RValue rValue) {
        if (rValue instanceof Value) {
            return (Value) rValue;
        }
        if (rValue instanceof GlobalVariableCache.GlobalVar) {
            return globalVars.computeIfAbsent((GlobalVariableCache.GlobalVar) rValue, globalVar -> new UnknownValue("G_" + globalVars.size()));
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
