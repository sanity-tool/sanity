package ru.urururu.llvm.bitreader.codes;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import ru.urururu.llvm.bitreader.BlockId;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Codes {
    Map<Pair<Integer, Integer>, Codes> registry = new HashMap<>();

    static Codes get(int block, int code) {
        return registry.get(new ImmutablePair<>(block, code));
    }

    default void register(BlockId block, int code, Codes codeName) {
        Codes old = registry.put(new ImmutablePair<>(block.blockId, code), codeName);
        if (old != null) {
            throw new IllegalStateException("block = [" + block + "], code = [" + code + "], codeName = [" + codeName + "], oldCodeName = [" + old + ']');
        }
    }
}
