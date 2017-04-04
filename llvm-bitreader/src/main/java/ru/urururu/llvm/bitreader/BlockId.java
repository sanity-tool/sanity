package ru.urururu.llvm.bitreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public enum BlockId {
    BLOCKINFO(0),
    /**
     * This is the top-level block that contains the entire module, and describes a variety of per-module information.
     */
    MODULE_BLOCK(8),
    /**
     * This enumerates the parameter attributes.
     */
    PARAMATTR_BLOCK(9),
    /**
     * This describes the attribute group table.
     */
    PARAMATTR_GROUP_BLOCK(10),
    /**
     * This describes constants for a module or function.
     */
    CONSTANTS_BLOCK(11),
    /**
     * This describes a function body.
     */
    FUNCTION_BLOCK(12),
    /**
     * This describes a value symbol table.
     */
    VALUE_SYMTAB_BLOCK(14),
    /**
     * This describes metadata items.
     */
    METADATA_BLOCK(15),
    /**
     * This contains records associating metadata with function instruction values.
     */
    METADATA_ATTACHMENT(16),
    /**
     * This describes all of the types in the module.
     */
    TYPE_BLOCK(17),
    ;

    public final int blockId;
    private static final Map<Integer, BlockId> blockIds = new HashMap<>();

    BlockId(int blockId) {
        this.blockId = blockId;
    }

    static {
        for (BlockId blockId : BlockId.values()) {
            register(blockId.blockId, blockId);
        }
    }

    private static void register(int blockId, BlockId id) {
        blockIds.put(blockId, id);
    }

    public static BlockId get(int blockId) {
        return blockIds.get(blockId);
    }
}
