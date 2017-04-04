package ru.urururu.sanity.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.AbstractBytecodeParser;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.llvm.*;
import ru.urururu.util.Iterables;

import java.io.IOException;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBytecodeParser extends AbstractBytecodeParser<SWIGTYPE_p_LLVMOpaqueModule,
        SWIGTYPE_p_LLVMOpaqueType, SWIGTYPE_p_LLVMOpaqueValue, SWIGTYPE_p_LLVMOpaqueValue,
        SWIGTYPE_p_LLVMOpaqueBasicBlock, NativeCfgBuildingCtx> {
    @Autowired
    NativeParsersFacade parsers;

    @Override
    protected Iterable<SWIGTYPE_p_LLVMOpaqueValue> getGlobals(SWIGTYPE_p_LLVMOpaqueModule module) {
        return Iterables.linked(bitreader.LLVMGetFirstGlobal(module), bitreader::LLVMGetNextGlobal);
    }

    protected void parseGlobalInitializer(CfgBuilder builder, SWIGTYPE_p_LLVMOpaqueValue initializer, LValue globalToInitialize) {
        if (bitreader.LLVMIsAConstantStruct(initializer) != null) {
            int n = bitreader.LLVMGetNumOperands(initializer);
            while (n-- > 0) {
                SWIGTYPE_p_LLVMOpaqueValue fieldInit = bitreader.LLVMGetOperand(initializer, n);
                RValue rValue = parsers.parseRValue(null, fieldInit);
                builder.append(new Assignment(new Indirection(new GetFieldPointer(globalToInitialize, n)), rValue, null));
            }
        } else if (bitreader.LLVMIsAConstantArray(initializer) != null) {
            int n = bitreader.LLVMGetNumOperands(initializer);
            while (n-- > 0) {
                SWIGTYPE_p_LLVMOpaqueValue elementInit = bitreader.LLVMGetOperand(initializer, n);
                RValue rValue = parsers.parseRValue(null, elementInit);
                builder.append(new Assignment(new Indirection(new GetElementPointer(globalToInitialize, constants.get(n, parsers.parse(bitreader.LLVMIntType(32))))), rValue, null));
            }
        } else if (bitreader.LLVMIsAConstantDataArray(initializer) != null) {
            Type type = parsers.parse(bitreader.LLVMTypeOf(initializer));
            String s = bitreader.GetDataArrayString(initializer);
            if (s != null) {
                builder.append(new Assignment(globalToInitialize, constants.get(s, type), null));
            }
        } else {
            builder.append(new Assignment(globalToInitialize, parsers.parseRValue(null, initializer), null));
        }
    }

    @Override
    protected SWIGTYPE_p_LLVMOpaqueValue getInitializer(SWIGTYPE_p_LLVMOpaqueValue global) {
        return bitreader.LLVMGetInitializer(global);
    }

    @Override
    protected SWIGTYPE_p_LLVMOpaqueModule parseModule(String absolute) throws IOException {
        SWIGTYPE_p_LLVMOpaqueModule module = bitreader.parse(absolute);

        if (module == null) {
            throw new IOException("module == null"); // todo get error message from llvm internals?
        }

        return module;
    }

    @Override
    protected Iterable<SWIGTYPE_p_LLVMOpaqueValue> getFunctions(SWIGTYPE_p_LLVMOpaqueModule module) {
        return Iterables.linked(bitreader.LLVMGetFirstFunction(module), bitreader::LLVMGetNextFunction);
    }

    @Override
    protected Iterable<SWIGTYPE_p_LLVMOpaqueBasicBlock> getBlocks(SWIGTYPE_p_LLVMOpaqueValue function) {
        return Iterables.linked(bitreader.LLVMGetFirstBasicBlock(function), bitreader::LLVMGetNextBasicBlock);
    }

    @Override
    protected String toDebugString(SWIGTYPE_p_LLVMOpaqueValue value) {
        return bitreader.LLVMPrintValueToString(value);
    }

    @Override
    protected SWIGTYPE_p_LLVMOpaqueValue toValue(SWIGTYPE_p_LLVMOpaqueBasicBlock block) {
        return bitreader.LLVMBasicBlockAsValue(block);
    }

    @Override
    protected NativeCfgBuildingCtx createCtx(SWIGTYPE_p_LLVMOpaqueValue function) {
        return new NativeCfgBuildingCtx(parsers, function);
    }

    @Override
    protected void releaseModule(SWIGTYPE_p_LLVMOpaqueModule module) {
        bitreader.LLVMDisposeModule(module);
    }
}
