package na.okutane.cpp;

import na.okutane.api.Cfg;
import na.okutane.cpp.llvm.BasicBlock;
import na.okutane.cpp.llvm.DILocation;
import na.okutane.cpp.llvm.DIScope;
import na.okutane.cpp.llvm.DebugLoc;
import na.okutane.cpp.llvm.Function;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.MDNode;
import na.okutane.cpp.llvm.Module;
import na.okutane.cpp.llvm.StringRef;
import na.okutane.cpp.llvm.bitreader;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.matveev@corp.mail.ru">Dmitriy Matveev</a>
 */
public class Parser {
    static {
        System.loadLibrary("irreader");
    }

    public Cfg[] parse(String filename) {
        try {

            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File("/Users/jondoe/IdeaProjects/SA/sanity/tests"));
            //pb.command("clang", filename, "-c", "-S", "-emit-llvm", "-gline-tables-only");

            pb.command("clang", filename, "-c", "-emit-llvm", "-gline-tables-only", "-o", "result.bc");

            pb.inheritIO();

            Process process = pb.start();

            int result = process.waitFor();

            if (result == 0) {
                Module m = bitreader.parse("/Users/jondoe/IdeaProjects/SA/sanity/tests/result.bc");
                m.dump();

                int size = bitreader.getModuleFunctionsSize(m);
                System.out.println("count: " + size);
                for (int i = 0; i < size; i++) {
                    Function function = bitreader.getModuleFunctionsItem(m, i);
                    function.Materialize();

                    BasicBlock entryBlock = function.getEntryBlock();

                    //entryBlock.getInstList()

                    processBlock(m, entryBlock);
                    //function.getEntryBlock();
                }
                //m.dump();
                m.delete();
                return null;
            } else {
                System.out.println("result: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processBlock(Module m, BasicBlock entryBlock) {
        for (int i = 0; i < bitreader.getBasicBlockInstructionsSize(entryBlock); i++) {
            processInstruction(m, bitreader.getBasicBlockInstructionsItem(entryBlock, i));
        }
    }

    private void processInstruction(Module m, Instruction inst) {
        MDNode node = inst.getMetadata(new StringRef("dbg"));
        if (node != null) {
            DILocation loc = new DILocation(node);
            long Line = loc.getLineNumber();
            StringRef File = loc.getFilename();
            StringRef Dir = loc.getDirectory();
            System.out.println(File.begin());
        }

        DebugLoc debugLoc = inst.getDebugLoc();
        MDNode scope = debugLoc.getScope(m.getContext());
        DIScope scoped = new DIScope(scope);
        //scoped.
        System.out.println(inst.getOpcodeName() + " // " + scoped.getFilename().begin() + ':' + debugLoc.getLine());
    }
}
