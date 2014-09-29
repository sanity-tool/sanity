package na.okutane.cpp;

import na.okutane.api.cfg.SourceRange;
import na.okutane.cpp.llvm.ConstantInt;
import na.okutane.cpp.llvm.Instruction;
import na.okutane.cpp.llvm.MDNode;
import na.okutane.cpp.llvm.StringRef;
import na.okutane.cpp.llvm.Value;
import na.okutane.cpp.llvm.bitreader;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class SourceRangeFactory {
    public SourceRange getSourceRange(Instruction instruction) {
        MDNode node = instruction.getMetadata(new StringRef("dbg"));
        if (node != null) {
            //deepDump(node);
            String filename = getFilename(node);
            String directory = getDirectory(node);
            int lineNo = toInt(node.getOperand(0));
            if (filename != null) {
                if (new File(filename).isAbsolute()) {
                    return new SourceRange(filename, lineNo);
                }
                return new SourceRange(new File(directory, filename).getAbsolutePath(), lineNo);
            }
        }
        return null;
    }

    private String getFilename(MDNode node) {
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

    private String getDirectory(MDNode node) {
        Value maybeTag = node.getOperand(0);
        if (ConstantInt.classof(maybeTag)) {
            int val = toInt(maybeTag);
            if (val == 786473) {
                return bitreader.toMDString(node.getOperand(2)).begin();
            } else {
                return getDirectory(bitreader.toMDNode(node.getOperand(2)));
            }
        }
        return null;
    }

    private int toInt(Value value) {
        String s = bitreader.toConstantInt(value).getValue().toString(10, true);
        return Integer.parseInt(s, 10);
    }
}
