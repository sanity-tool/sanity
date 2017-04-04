package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.globals.GlobalVariable;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.ArrayConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.StructureConstant;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.*;
import com.oracle.truffle.llvm.parser.model.visitors.FunctionVisitor;
import com.oracle.truffle.llvm.parser.model.visitors.ModelVisitor;
import com.oracle.truffle.llvm.runtime.types.IntegerType;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.util.Iterators;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CfgBuilder implements ModelVisitor {
    private final SulongParsersFacade parsers;
    private final List<Cfg> result;

    private ConstCache constCache = new ConstCache();

    Cfe initFirst = null;
    Cfe initLast = null;

    public CfgBuilder(SulongParsersFacade parsers, List<Cfg> result) {
        this.parsers = parsers;
        this.result = result;
    }

    @Override
    public void visit(FunctionDefinition function) {
        SuCfgBuildingCtx ctx = new SuCfgBuildingCtx(parsers, function);

        Cfe[] functionEntry = new Cfe[1];

        function.accept(new FunctionVisitor() {
            @Override
            public void visit(InstructionBlock block) {
                Cfe blockEntry = processBlock(ctx, block);
                Cfe label = ctx.getLabel(block);
                label.setNext(blockEntry);

                append(new Cfe[]{blockEntry});
            }

            private void append(Cfe[] block) {
                if (functionEntry[0] == null) {
                    functionEntry[0] = block[0];
                }
            }
        });

        Cfe entry = functionEntry[0];

        entry = new CfgUtils().removeNoOps(entry);

        result.add(new Cfg(fixName(function.getName()), entry));
    }

    public Cfe getInitFirst() {
        return initFirst;
    }

    @Override
    public void visit(GlobalVariable variable) {
        Symbol initializer = variable.getValue();
        if (initializer == null) {
            return;
        }

        LValue globalToInitialize = new Indirection(parsers.parseLValue(null, variable));

        if (initializer instanceof StructureConstant) {
            Cfe cfe = null;
            int n = ((StructureConstant) initializer).getElementCount();
            while (n-- > 0) {
                Symbol fieldInit = ((StructureConstant) initializer).getElement(n);
                RValue rValue = parsers.parseRValue(null, fieldInit);

                Cfe fieldInitCfe = new Assignment(new Indirection(new GetFieldPointer(globalToInitialize, n)), rValue, null);
                fieldInitCfe.setNext(cfe);
                cfe = fieldInitCfe;
            }

            append(cfe);
            return;
        }

        if (initializer instanceof ArrayConstant) {
            Cfe cfe = null;
            int n = ((ArrayConstant) initializer).getElementCount();
            while (n-- > 0) {
                Symbol fieldInit = ((ArrayConstant) initializer).getElement(n);
                RValue rValue = parsers.parseRValue(null, fieldInit);

                Cfe fieldInitCfe = new Assignment(new Indirection(new GetElementPointer(globalToInitialize, constCache.get(n, parsers.parse(IntegerType.INTEGER)))), rValue, null);
                fieldInitCfe.setNext(cfe);
                cfe = fieldInitCfe;
            }

            append(cfe);
            return;
        }

        try {
            RValue right = parsers.parseRValue(null, initializer);

            if (right != null) {
                append(new Assignment(globalToInitialize, right, null));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    void append(Cfe cfe) {
        if (initFirst == null) {
            initFirst = initLast = cfe;
        } else if (cfe != null) {
            initLast.setNext(cfe);
            initLast = cfe;
        }
    }

    private Cfe processBlock(CfgBuildingCtx<Type, Symbol, InstructionBlock> ctx, InstructionBlock entryBlock) {
        ctx.enterSubCfg(entryBlock);

        Cfe first = null;
        Cfe last = null;

        Iterator<Instruction> instructions = Iterators.indexed(entryBlock::getInstruction, entryBlock::getInstructionCount);
        while (instructions.hasNext()) {
            Cfe cfe = parsers.parse(ctx, instructions.next());
            if (first == null) {
                first = last = cfe;
            } else if (cfe != null) {
                last.setNext(cfe);
                last = last.getNext();
            }
        }

        return first;
    }

    private String fixName(String name) {
        if (name.startsWith("@")) {
            return name.substring(1);
        }

        return name;
    }
}
