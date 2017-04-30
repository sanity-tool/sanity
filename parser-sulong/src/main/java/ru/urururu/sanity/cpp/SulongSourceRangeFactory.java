package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.metadata.*;
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

        long[] line = new long[1];
        String[] file = new String[1];

        debugLocation.accept(new MetadataVisitor() {
            @Override
            public void ifVisitNotOverwritten(MDBaseNode alias) {
                throw new IllegalStateException(alias.getClass().getSimpleName());
            }

            @Override
            public void visit(MDReference alias) {
                alias.get().accept(this);
            }

            @Override
            public void visit(MDLocation alias) {
                line[0] = alias.getLine();
                alias.getScope().accept(this);
            }

            @Override
            public void visit(MDSubprogram alias) {
                alias.getFile().accept(this);
            }

            @Override
            public void visit(MDLexicalBlock alias) {
                alias.getFile().accept(this);
            }

            @Override
            public void visit(MDFile alias) {
                file[0] = alias.asFile().getAbsolutePath();
            }
        });

        if (file[0] == null) {
            return null;
        }

        return new SourceRange(file[0], line[0]);
    }

}
