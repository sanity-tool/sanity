package na.okutane.cpp;

import na.okutane.api.Cfg;
import na.okutane.api.cfg.Cfe;
import na.okutane.cpp.llvm.BasicBlock;
import na.okutane.cpp.llvm.Function;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.Module;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class Parser {
    @Autowired
    InstructionParser instructionParser;

    static {
        System.loadLibrary("irreader");
    }

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
                Module m = bitreader.parse(objFile.getAbsolutePath());
                //m.dump();

                int size = bitreader.getModuleFunctionsSize(m);
                System.out.println("count: " + size);
                ArrayList<Cfg> result = new ArrayList<Cfg>(size);
                for (int i = 0; i < size; i++) {
                    Function function = bitreader.getModuleFunctionsItem(m, i);
                    if (function.isMaterializable()) {
                        function.Materialize();

                        BasicBlock entryBlock = function.getEntryBlock();

                        Cfe entry = processBlock(entryBlock);

                        result.add(new Cfg(bitreader.getName(function), entry));
                    }
                }
                m.dump();
                m.delete();
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

    private Cfe processBlock(BasicBlock entryBlock) {
        Cfe first = null;
        Cfe last = null;
        for (int i = 0; i < bitreader.getBasicBlockInstructionsSize(entryBlock); i++) {
            Instruction instruction = bitreader.getBasicBlockInstructionsItem(entryBlock, i);
            Cfe cfe = instructionParser.parse(instruction);
            if (first == null) {
                first = last = cfe;
            } else if (cfe != null) {
                last.setNext(cfe);
                last = last.getNext();
            }
        }

        return first;
    }
}
