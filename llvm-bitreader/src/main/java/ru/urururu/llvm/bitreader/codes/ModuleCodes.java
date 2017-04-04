package ru.urururu.llvm.bitreader.codes;

import ru.urururu.llvm.bitreader.BlockId;

/**
 * MODULE blocks have a number of optional fields and subblocks.
 *
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */

public enum ModuleCodes implements Codes {
    /**
     * VERSION:     [version#]
     */
    MODULE_CODE_VERSION(1),
    /**
     * TRIPLE:      [strchr x N]
     */
    MODULE_CODE_TRIPLE(2),
    /**
     * DATALAYOUT:  [strchr x N]
     */
    MODULE_CODE_DATALAYOUT(3),
    /**
     * ASM:         [strchr x N]
     */
    MODULE_CODE_ASM(4),
    /**
     * SECTIONNAME: [strchr x N]
     */
    MODULE_CODE_SECTIONNAME(5),

    // FIXME: Remove DEPLIB in 4.0.
    /**
     * DEPLIB:      [strchr x N]
     */
    MODULE_CODE_DEPLIB(6), //

    /**
     * GLOBALVAR: [pointer type, isconst, initid, linkage, alignment, section, visibility, threadlocal]
     */
    MODULE_CODE_GLOBALVAR(7),

    /**
     * FUNCTION:  [type, callingconv, isproto, linkage, paramattrs, alignment, section, visibility, gc, unnamed_addr]
     */
    MODULE_CODE_FUNCTION(8),

    /**
     * ALIAS: [alias type, aliasee val#, linkage, visibility]
     */
    MODULE_CODE_ALIAS_OLD(9),

    /**
     * MODULE_CODE_PURGEVALS: [numvals]
     */
    MODULE_CODE_PURGEVALS(10),

    /**
     * GCNAME: [strchr x N]
     */
    MODULE_CODE_GCNAME(11),
    /**
     * COMDAT: [selection_kind, name]
     */
    MODULE_CODE_COMDAT(12),

    /**
     * VSTOFFSET: [offset]
     */
    MODULE_CODE_VSTOFFSET(13),

    /**
     * ALIAS: [alias value type, addrspace, aliasee val#, linkage, visibility]
     */
    MODULE_CODE_ALIAS(14),

    MODULE_CODE_METADATA_VALUES_UNUSED(15),

    /**
     * SOURCE_FILENAME: [namechar x N]
     */
    MODULE_CODE_SOURCE_FILENAME(16),

    /**
     * HASH: [5*i32]
     */
    MODULE_CODE_HASH(17),

    /**
     * IFUNC: [ifunc value type, addrspace, resolver val#, linkage, visibility]
     */
    MODULE_CODE_IFUNC(18),;

    ModuleCodes(int code) {
        register(BlockId.MODULE_BLOCK, code, this);
    }
}
