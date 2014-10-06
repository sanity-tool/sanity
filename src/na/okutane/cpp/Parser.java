package na.okutane.cpp;

import na.okutane.api.Cfg;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class Parser {
    @Autowired
    InstructionParser instructionParser;

    public List<Cfg> parse(String filename) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File("/Users/jondoe/IdeaProjects/SA/sanity/tests"));
            //pb.command("clang", filename, "-c", "-S", "-emit-llvm", "-gline-tables-only");

            File objFile = File.createTempFile("result", ".bc");
            pb.command(getClangParameters(filename, objFile.getAbsolutePath()));

            pb.inheritIO();

            Process process = pb.start();

            int resultCode = process.waitFor();

            if (resultCode == 0) {

                SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(objFile.getAbsolutePath());

                if (m == null) {
                    return Collections.emptyList();
                }

                bitreader.LLVMDumpModule(m);

                ArrayList<Cfg> result = new ArrayList<Cfg>();

                SWIGTYPE_p_LLVMOpaqueValue function = bitreader.LLVMGetFirstFunction(m);

                while (function != null) {
                    if (bitreader.LLVMGetFirstBasicBlock(function) != null) {
                        CfgBuildingCtx ctx = new CfgBuildingCtx();

                        SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock = bitreader.LLVMGetEntryBasicBlock(function);

                        Cfe entry = processBlock(ctx, entryBlock);

                        result.add(new Cfg(bitreader.LLVMGetValueName(function), entry));
                    }
                    function = bitreader.LLVMGetNextFunction(function);
                }
                bitreader.LLVMDumpModule(m);
                bitreader.LLVMDisposeModule(m);
                objFile.delete();
                return result;
            } else {
                System.out.println("result: " + resultCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] getClangParameters(String filename, String objFile) {
        List<String> parameters = new ArrayList<String>();

        if (filename.endsWith(".c")) {
            parameters.add("clang");
        } else {
            parameters.add("clang++");
        }

        parameters.addAll(Arrays.asList(filename, "-c", "-emit-llvm", "-femit-all-decls", "-gline-tables-only", "-o", objFile));

        return parameters.toArray(new String[parameters.size()]);
    }

    private Cfe processBlock(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock) {
        Cfe first = null;
        Cfe last = null;

        SWIGTYPE_p_LLVMOpaqueValue instruction = bitreader.LLVMGetFirstInstruction(entryBlock);
        while (instruction != null) {
            Cfe cfe = instructionParser.parse(ctx, instruction);
            if (first == null) {
                first = last = cfe;
            } else if (cfe != null) {
                last.setNext(cfe);
                last = last.getNext();
            }
            instruction = bitreader.LLVMGetNextInstruction(instruction);
        }

        return first;
    }
}
