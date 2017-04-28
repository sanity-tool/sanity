package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.metadata.MDBaseNode;
import com.oracle.truffle.llvm.parser.metadata.MDLocation;
import com.oracle.truffle.llvm.parser.metadata.MetadataVisitor;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.SourceRangeFactory;
import ru.urururu.sanity.api.cfg.SourceRange;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongSourceRangeFactory extends SourceRangeFactory<Instruction> {
    @Override
    public SourceRange getSourceRange(Instruction instruction) {
        return getSourceRange(instruction.getDebugLocation());
    }

    private SourceRange getSourceRange(MDLocation debugLocation) {
        if (debugLocation == null) {
            return null;
        }

        int[] line = new int[1];
        String[] file = new String[1];

        debugLocation.accept(new MetadataVisitor() {
            @Override
            public void ifVisitNotOverwritten(MDBaseNode alias) {
                throw new IllegalStateException(alias.getClass().getSimpleName());
            }
        });

        if (file[0] == null) {
            return null;
        }

        return new SourceRange(file[0], line[0]);
    }

}
