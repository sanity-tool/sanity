package ru.urururu.sanity.cpp;

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.llvm.parser.BitcodeParserResult;
import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.parser.model.blocks.InstructionBlock;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDeclaration;
import com.oracle.truffle.llvm.parser.model.functions.FunctionDefinition;
import com.oracle.truffle.llvm.parser.model.globals.GlobalVariable;
import com.oracle.truffle.llvm.parser.model.symbols.constants.*;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.ArrayConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.StructureConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.aggregate.VectorConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.DoubleConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.FloatConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.floatingpoint.X86FP80Constant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.BigIntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.constants.integer.IntegerConstant;
import com.oracle.truffle.llvm.parser.model.symbols.instructions.Instruction;
import com.oracle.truffle.llvm.parser.model.visitors.ConstantVisitor;
import com.oracle.truffle.llvm.parser.model.visitors.ModelVisitor;
import com.oracle.truffle.llvm.runtime.LLVMLanguage;
import com.oracle.truffle.llvm.runtime.types.symbols.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.AbstractBytecodeParser;
import ru.urururu.sanity.api.cfg.CfgBuilder;
import ru.urururu.sanity.api.cfg.LValue;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongBytecodeParser extends AbstractBytecodeParser<ModelModule, com.oracle.truffle.llvm.runtime.types.Type,
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
        if (initializer instanceof Constant) {
            ((Constant) initializer).accept(new ConstantVisitor() {
                @Override
                public void visit(ArrayConstant arrayConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(StructureConstant structureConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(VectorConstant vectorConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(BigIntegerConstant bigIntegerConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(BinaryOperationConstant binaryOperationConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(BlockAddressConstant blockAddressConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(CastConstant castConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(CompareConstant compareConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(DoubleConstant doubleConstant) {
                    addSimpleInitializer(builder, initializer, globalToInitialize);
                }

                @Override
                public void visit(FloatConstant floatConstant) {
                    addSimpleInitializer(builder, initializer, globalToInitialize);
                }

                @Override
                public void visit(X86FP80Constant x86fp80Constant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(FunctionDeclaration functionDeclaration) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(FunctionDefinition functionDefinition) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(GetElementPointerConstant getElementPointerConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(InlineAsmConstant inlineAsmConstant) {
                    throw new NotImplementedException();
                }

                @Override
                public void visit(IntegerConstant integerConstant) {
                    addSimpleInitializer(builder, initializer, globalToInitialize);
                }

                @Override
                public void visit(NullConstant nullConstant) {
                    addSimpleInitializer(builder, initializer, globalToInitialize);
                }

                @Override
                public void visit(StringConstant stringConstant) {
                    addSimpleInitializer(builder, initializer, globalToInitialize);
                }

                @Override
                public void visit(UndefinedConstant undefinedConstant) {
                    throw new NotImplementedException();
                }
            });

            return;
        }

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
