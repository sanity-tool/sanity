package na.okutane.cpp;

import na.okutane.api.Cfg;
import na.okutane.cpp.llvm.BasicBlock;
import na.okutane.cpp.llvm.ConstantInt;
import na.okutane.cpp.llvm.Function;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.MDNode;
import na.okutane.cpp.llvm.MDString;
import na.okutane.cpp.llvm.Module;
import na.okutane.cpp.llvm.StringRef;
import na.okutane.cpp.llvm.Value;
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
                //m.dump();

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
                m.dump();
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
        String filename = null;
        Integer lineNo;
        if (node != null) {
            //deepDump(node);
            filename = getFilename(node) + ':' + toInt(node.getOperand(0));
            lineNo = toInt(node.getOperand(0));
        }

        System.out.println(inst.getOpcodeName() + " // " + filename);
    }

    private void deepDump(MDNode node) {
        node.dump();

        if (node.getNumOperands() > 2) {
            Value operand = node.getOperand(2);
            if (MDNode.classof(operand)) {
                deepDump(bitreader.toMDNode(operand));
            } else if (MDString.classof(operand)) {
                System.err.println(bitreader.toMDString(operand).begin());
            }
        }
    }

    String getFilename(MDNode node) {
        Value maybeTag = node.getOperand(0);
        if (ConstantInt.classof(maybeTag)) {
            int val = toInt(maybeTag);
            if (val == 786473) {
                return bitreader.toMDString(node.getOperand(1)).begin();
            } else {
                return getFilename(bitreader.toMDNode(node.getOperand(2)));
            }
        }
        return null;
    }

    private int toInt(Value value) {
        String s = bitreader.toConstantInt(value).getValue().toString(10, true);
        return Integer.parseInt(s, 10);
    }
}
