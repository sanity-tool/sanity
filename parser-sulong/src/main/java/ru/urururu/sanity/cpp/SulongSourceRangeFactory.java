package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.runtime.types.MetadataVisitor;
import com.oracle.truffle.llvm.runtime.types.metadata.*;
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

    private SourceRange getSourceRange(MetadataBaseNode debugLocation) {
        if (debugLocation == null) {
            return null;
        }

        int[] line = new int[1];
        String[] file = new String[1];

        debugLocation.accept(new MetadataVisitor() {
            @Override
            public void ifVisitNotOverwritten(MetadataBaseNode alias) {
                throw new IllegalStateException();
            }

            @Override
            public void visit(MetadataSubprogram alias) {
                alias.getFile().get().accept(this);
            }

            @Override
            public void visit(MetadataCompileUnit alias) {
                alias.getFile().get().accept(this);
            }

            @Override
            public void visit(MetadataFile alias) {
                alias.getFile().get().accept(this);
            }

            @Override
            public void visit(MetadataString alias) {
                file[0] = alias.getString();
            }

            @Override
            public void visit(MetadataLexicalBlock alias) {
                alias.getFile().get().accept(this);
            }

            @Override
            public void visit(MetadataCompositeType alias) {
                alias.getFile().get().accept(this);
            }

//            @Override
//            public void visit(MetadataDebugLocation alias) {
//                line[0] = Math.toIntExact(alias.getLine());
//                alias.getScope().accept(this);
//            }
        });

        if (file[0] == null) {
            return null;
        }

        return new SourceRange(file[0], line[0]);
    }

}
