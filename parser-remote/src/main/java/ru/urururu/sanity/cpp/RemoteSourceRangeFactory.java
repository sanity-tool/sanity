package ru.urururu.sanity.cpp;

import io.swagger.client.model.ModuleDto;
import io.swagger.client.model.ValueRefDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.SourceRangeFactory;
import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteSourceRangeFactory extends SourceRangeFactory<ValueRefDto> implements ParserListener {
    private ModuleDto currentModule;

    public SourceRange getSourceRange(ValueRefDto instruction) {
        if (instruction.getKind() != ValueRefDto.KindEnum.INSTRUCTION) {
            return null;
        }

        

        int line = bitreader.SAGetInstructionDebugLocLine(instruction);
        if (versionByte == 3) {
            if (line != -1) {
                String filename = bitreader.SAGetInstructionDebugLocScopeFile(instruction);
                return getSourceRange(filename, line);
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
                if (new File(filename).isAbsolute()) {
                    return getSourceRange(filename, line);
                }
                return getSourceRange(new File(directory, filename).getAbsolutePath(), line);
            }
        }

        return null;
    }

    @Override
    public void onModuleStarted(ModuleDto module) {
        this.currentModule = module;
    }
}
