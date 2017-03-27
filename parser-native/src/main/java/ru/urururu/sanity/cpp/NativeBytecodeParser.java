package ru.urururu.sanity.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;
import ru.urururu.util.Iterables;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class NativeBytecodeParser implements BytecodeParser {
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    InstructionParser instructionParser;
    @Autowired
    ParsersFacade parsers;
    @Autowired
    TypeParser typeParser;
    @Autowired
    ValueParser valueParser;
    @Autowired
    ParserListener[] listeners;
    @Autowired
    ConstCache constants;

    private Cfe parseGlobalInitializers(SWIGTYPE_p_LLVMOpaqueModule module) {
        Cfe first = null;
        Cfe last = null;

        Iterable<SWIGTYPE_p_LLVMOpaqueValue> globals =
                    Iterables.linked(() -> bitreader.LLVMGetFirstGlobal(module), bitreader::LLVMGetNextGlobal);
        for (SWIGTYPE_p_LLVMOpaqueValue global : globals) {
            try {
                SWIGTYPE_p_LLVMOpaqueValue initializer = bitreader.LLVMGetInitializer(global);
                if (initializer != null) {
                    Cfe cfe;
                    GlobalVar pointerToGlobal = (GlobalVar) valueParser.parseLValue(null, global);

                    if (pointerToGlobal.getName().contains("rustc_debug")) {
                        continue;
                    }

                    LValue globalToInitialize = new Indirection(pointerToGlobal);
                    if (bitreader.LLVMIsAConstantStruct(initializer) != null) {
                        cfe = null;
                        int n = bitreader.LLVMGetNumOperands(initializer);
                        while (n-- > 0) {
                            SWIGTYPE_p_LLVMOpaqueValue fieldInit = bitreader.LLVMGetOperand(initializer, n);
                            RValue rValue = valueParser.parseRValue(null, fieldInit);

                            Cfe fieldInitCfe = new Assignment(new Indirection(new GetFieldPointer(globalToInitialize, n)), rValue, null);
                            fieldInitCfe.setNext(cfe);
                            cfe = fieldInitCfe;
                        }
                    } else if (bitreader.LLVMIsAConstantArray(initializer) != null) {
                        cfe = null;
                        int n = bitreader.LLVMGetNumOperands(initializer);
                        while (n-- > 0) {
                            SWIGTYPE_p_LLVMOpaqueValue elementInit = bitreader.LLVMGetOperand(initializer, n);
                            RValue rValue = valueParser.parseRValue(null, elementInit);

                            Cfe fieldInitCfe = new Assignment(new Indirection(new GetElementPointer(globalToInitialize, constants.get(n, typeParser.parse(bitreader.LLVMIntType(32))))), rValue, null);
                            fieldInitCfe.setNext(cfe);
                            cfe = fieldInitCfe;
                        }
                    } else if (bitreader.LLVMIsAConstantDataArray(initializer) != null) {
                        Type type = typeParser.parse(bitreader.LLVMTypeOf(initializer));
                        String s = bitreader.GetDataArrayString(initializer);
                        if (s != null) {
                            cfe = new Assignment(globalToInitialize, constants.get(s, type), null);
                        } else {
                            cfe = null;
                        }
                    } else {
                        cfe = new Assignment(globalToInitialize, valueParser.parseRValue(null, initializer), null);
                    }
                    if (first == null) {
                        first = last = cfe;
                    } else if (cfe != null) {
                        last.setNext(cfe);
                        last = last.getNext();
                    }
                }
            } catch (Exception e) {
                System.err.println("Can't parse global: " + bitreader.LLVMGetValueName(global));
                e.printStackTrace(System.err);
            }
        }

        return first;
    }

    private Cfe processBlock(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock) {
        ctx.enterSubCfg(ctx, entryBlock);

        Cfe first = null;
        Cfe last = null;

        SWIGTYPE_p_LLVMOpaqueValue instruction = bitreader.LLVMGetFirstInstruction(entryBlock);
        while (instruction != null) {
            Cfe cfe = instructionParser.parse(ctx, instruction);
            if (first == null) {
                first = last = cfe;
            } else if (cfe != null) {
                last.setNext(cfe);
                last = last.getNext();
            }
            instruction = bitreader.LLVMGetNextInstruction(instruction);
        }

        return first;
    }

    @Override
    public List<Cfg> parse(File file) {
        SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(file.getAbsolutePath());

        if (m == null) {
            return Collections.emptyList();
        }

        //bitreader.LLVMDumpModule(m); // todo move to separate listener for test/debug
        for (ParserListener listener : listeners) {
            listener.onModuleStarted(m);
        }

        try {
            ArrayList<Cfg> result = new ArrayList<>();

            SWIGTYPE_p_LLVMOpaqueValue function = bitreader.LLVMGetFirstFunction(m);
            while (function != null) {
                try {
                    if (bitreader.LLVMGetFirstBasicBlock(function) != null) {
                        CfgBuildingCtx ctx = new CfgBuildingCtx(parsers, function);

                        SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock = bitreader.LLVMGetEntryBasicBlock(function);

                        Cfe entry = processBlock(ctx, entryBlock);

                        SWIGTYPE_p_LLVMOpaqueBasicBlock block = bitreader.LLVMGetFirstBasicBlock(function);
                        block = bitreader.LLVMGetNextBasicBlock(block);
                        while (block != null) {
                            Cfe blockEntry = processBlock(ctx, block);
                            Cfe label = ctx.getLabel(bitreader.LLVMBasicBlockAsValue(block));

                            label.setNext(blockEntry);

                            block = bitreader.LLVMGetNextBasicBlock(block);
                        }

                        entry = cfgUtils.removeNoOps(entry);

                        result.add(new Cfg((FunctionAddress) valueParser.parseRValue(null, function), entry));
                    }
                } catch (Exception e) {
                    System.err.println("Can't parse function: " + bitreader.LLVMGetValueName(function));
                    e.printStackTrace(System.err);
                }

                function = bitreader.LLVMGetNextFunction(function);
            }

            Cfe entry = parseGlobalInitializers(m);
            if (entry != null) {
                result.add(new Cfg("<module init>", entry));
            }

            return result;
        } finally {
            for (ParserListener listener : listeners) {
                listener.onModuleFinished(m);
            }
            bitreader.LLVMDisposeModule(m);
        }
    }
}
