package ru.urururu.llvm.bitreader.codes;

import ru.urururu.llvm.bitreader.BlockId;

/**
 * Value symbol table codes.
 *
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public enum ValueSymtabCodes implements Codes {
    /**
     * VST_ENTRY: [valueid, namechar x N]
     */
    VST_CODE_ENTRY(1),
    /**
     * VST_BBENTRY: [bbid, namechar x N]
     */
    VST_CODE_BBENTRY(2),
    /**
     * VST_FNENTRY: [valueid, offset, namechar x N]
     */
    VST_CODE_FNENTRY(3),

    /**
     * VST_COMBINED_ENTRY: [valueid, refguid]
     */
    VST_CODE_COMBINED_ENTRY(5),;

    ValueSymtabCodes(int code) {
        register(BlockId.VALUE_SYMTAB_BLOCK, code, this);
    }
};
