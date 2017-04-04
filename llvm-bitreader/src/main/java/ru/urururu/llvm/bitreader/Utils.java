package ru.urururu.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Utils {
    static char toChar(int char6code) {
        if (char6code < 0 || char6code > 63) {
            throw new IllegalArgumentException("c:" + char6code);
        }

        if (char6code <= 25) {
            return (char)('a' + char6code);
        }

        if (char6code <= 51) {
            return (char)('A' + char6code - 26);
        }

        if (char6code <= 61) {
            return (char)('0' + char6code - 52);
        }

        if (char6code == 62) {
            return '.';
        }

        if (char6code == 63) {
            return '_';
        }

        throw new IllegalArgumentException("c:" + char6code);
    }

    public static String toBlockId(int blockId) {
        BlockId block = BlockId.get(blockId);
        String name = block != null ? block.name() : "<unknown>";

        return blockId + " (" + name + ')';
    }
}
