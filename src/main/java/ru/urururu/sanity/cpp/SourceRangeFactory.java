package ru.urururu.sanity.cpp;

import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SourceRangeFactory implements ParserListener {
    public static final int DW_TAG_file_type = 786473;
    public static final int DW_TAG_lexical_block = 786443;
    public static final int DW_TAG_subprogram = 786478;

    private static final int FILE_INDEX = 0;
    private static final int DIRECTORY_INDEX = 1;

    private Long debugVersion;
    private Byte versionByte;

    public SourceRange getSourceRange(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        int line = bitreader.SAGetInstructionDebugLocLine(instruction);
        if (versionByte == 3) {
            if (line != -1) {
                String filename = bitreader.SAGetInstructionDebugLocScopeFile(instruction);
                if (filename != null) {
                    return new SourceRange(filename, line);
                }
                return null;
            }

            return null;
        }

        long id = bitreader.LLVMGetMDKindID("dbg", 3);
        SWIGTYPE_p_LLVMOpaqueValue node = bitreader.LLVMGetMetadata(instruction, id);

        if (node != null) {
            //deepDump(node);

            SWIGTYPE_p_LLVMOpaqueValue pair = getPair(node);

            if (pair != null) {
                String filename = bitreader.getMDString(bitreader.LLVMGetOperand(pair, 0));
                String directory = bitreader.getMDString(bitreader.LLVMGetOperand(pair, 1));
                int lineNo = line;
                if (new File(filename).isAbsolute()) {
                    return new SourceRange(filename, lineNo);
                }
                return new SourceRange(new File(directory, filename).getAbsolutePath(), lineNo);
            }
        }
        return null;
    }

    private SWIGTYPE_p_LLVMOpaqueValue getPair(SWIGTYPE_p_LLVMOpaqueValue node) {
        if (node == null) {
            return null;
        }
        int count = bitreader.LLVMGetNumOperands(node);
        if (count == 1) {
            return getPair(bitreader.LLVMGetOperand(node, 0));
        }
        if (bitreader.LLVMIsAMDNode(node) == null) {
            return null;
        }

        try {
            if (LlvmUtils.checkTag(node, DW_TAG_file_type) || LlvmUtils.checkTag(node, DW_TAG_lexical_block)) {
                return bitreader.LLVMGetOperand(node, 1);
            } else {
                return getPair(bitreader.LLVMGetOperand(node, 2));
            }
        } catch (IllegalArgumentException e) {
            for (int i = 0; i < count; i++) {
                System.out.println("bitreader.LLVMGetOperand(node, " + i + ") = " + bitreader.LLVMPrintValueToString(bitreader.LLVMGetOperand(node, i)));
            }
            throw e;
        }
    }

    @Override
    public void onModuleStarted(SWIGTYPE_p_LLVMOpaqueModule module) {
        debugVersion = bitreader.SAGetDebugMetadataVersionFromModule(module);
        versionByte = (byte)(debugVersion >>> 0); // most significat byte // todo report intellij bug here
        System.out.println("debugVersion = " + Long.toHexString(debugVersion));
        System.out.println("versionByte = " + versionByte);
    }

    @Override
    public void onModuleFinished(SWIGTYPE_p_LLVMOpaqueModule module) {
        debugVersion = null;
        versionByte = null;
    }
}
