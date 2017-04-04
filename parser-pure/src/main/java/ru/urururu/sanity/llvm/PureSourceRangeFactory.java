package ru.urururu.sanity.llvm;

import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.DebugLoc;
import ru.urururu.llvm.bitreader.Instruction;
import ru.urururu.sanity.api.SourceRangeFactory;
import ru.urururu.sanity.api.cfg.SourceRange;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureSourceRangeFactory extends SourceRangeFactory<Instruction> {
    @Override
    public SourceRange getSourceRange(Instruction instruction) {
        return getSourceRange(instruction.getDebugLoc());
    }

    private SourceRange getSourceRange(DebugLoc debugLoc) {
        if (debugLoc == null) {
            return null;
        }

        return getSourceRange(debugLoc.getScope().getFilename(), debugLoc.getLine());
    }

    private SourceRange getSourceRange(String filename, int line) {
        return new SourceRange(filename, line);
    }
}
