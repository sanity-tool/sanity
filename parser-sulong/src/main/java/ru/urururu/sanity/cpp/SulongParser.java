package ru.urururu.sanity.cpp;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.parser.BitcodeParserResult;
import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.globals.GlobalVariable;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.parser.model.visitors.ModelVisitor;
import com.oracle.truffle.llvm.runtime.LLVMLanguage;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.AbstractBytecodeParser;
import ru.urururu.sanity.api.cfg.CfgBuilder;
import ru.urururu.sanity.api.cfg.LValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongParser extends AbstractBytecodeParser<ModelModule, com.oracle.truffle.llvm.runtime.types.Type,
        Symbol, Instruction, InstructionBlock, SuCfgBuildingCtx> {
    @Autowired
    SulongParsersFacade parsers;

    @Override
    protected Iterable<? extends Symbol> getGlobals(ModelModule module) {
        List<Symbol> result = new ArrayList<>();

        module.accept(new ModelVisitor() {
            @Override
            public void visit(GlobalVariable variable) {
                result.add(variable);
            }
        });

        return result;
    }

    @Override
    protected void parseGlobalInitializer(CfgBuilder builder, Symbol initializer, LValue globalToInitialize) {
        addSimpleInitializer(builder, initializer, globalToInitialize);
    }

    @Override
    protected Symbol getInitializer(Symbol global) {
        GlobalVariable variable = (GlobalVariable) global;
        return variable.getValue();
    }

    @Override
    protected ModelModule parseModule(String absolute) throws IOException {
        Source source = Source.newBuilder(new File(absolute)).mimeType(LLVMLanguage.LLVM_BITCODE_MIME_TYPE).build();
        BitcodeParserResult bitcodeParserResult = BitcodeParserResult.getFromSource(source);

        return bitcodeParserResult.getModel();
    }

    @Override
    protected Iterable<? extends Symbol> getFunctions(ModelModule module) {
        List<Symbol> result = new ArrayList<>();

        module.accept(new ModelVisitor() {
            @Override
            public void visit(FunctionDefinition function) {
                result.add(function);
            }
        });

        return result;
    }

    @Override
    protected Iterable<InstructionBlock> getBlocks(Symbol function) {
        FunctionDefinition functionDefinition = (FunctionDefinition) function;
        return functionDefinition.getBlocks();
    }

    @Override
    protected String toDebugString(Symbol value) {
        return value.toString();
    }

    @Override
    protected Symbol toValue(InstructionBlock block) {
        return block;
    }

    @Override
    protected SuCfgBuildingCtx createCtx(Symbol function) {
        return new SuCfgBuildingCtx(parsers, (FunctionDefinition) function);
    }
}
