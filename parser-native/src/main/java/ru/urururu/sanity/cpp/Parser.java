package ru.urururu.sanity.cpp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;
import ru.urururu.sanity.utils.TempFileWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ParserImpl extends Parser {
    private static final Logger LOGGER = LoggerFactory.getLogger(Parser.class);

    @Autowired
    ClangParametersFactory parametersFactory;
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    InstructionParser instructionParser;
    @Autowired
    TypeParser typeParser;
    @Autowired
    ValueParser valueParser;
    @Autowired
    ParserListener[] listeners;
    @Autowired
    ConstCache constants;

    public List<Cfg> parse(String filename) throws ParseException {
        LOGGER.info("filename = {}", filename);
        try {
            try (TempFileWrapper objFile = new TempFileWrapper("result", ".bc")) {
                try (TempFileWrapper errFile = new TempFileWrapper("result", ".err")) {
                    String[] parameters = parametersFactory.getParameters(filename, objFile.getAbsolutePath());
                    LOGGER.info("parameters = {}", Arrays.toString(parameters));

                    ProcessBuilder pb = new ProcessBuilder(parameters);

                    pb.inheritIO();
                    pb.redirectError(ProcessBuilder.Redirect.to(errFile.getFile()));

                    Process process = pb.start();

                    int resultCode = process.waitFor();

                    if (resultCode == 0) {
                        SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(objFile.getAbsolutePath());

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
                                        CfgBuildingCtx ctx = new CfgBuildingCtx(typeParser, function);

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

                                        result.add(new Cfg(bitreader.LLVMGetValueName(function), entry));
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
                    } else {
                        String error = new String(Files.readAllBytes(Paths.get(errFile.getAbsolutePath())));
                        throw new ParseException(resultCode, error);
                    }
                }
            }
        } catch (InterruptedException | IOException e) {
            throw new ParseException(e);
        }
    }

    private Cfe parseGlobalInitializers(SWIGTYPE_p_LLVMOpaqueModule module) {
        Cfe first = null;
        Cfe last = null;

        SWIGTYPE_p_LLVMOpaqueValue global = bitreader.LLVMGetFirstGlobal(module);
        while (global != null) {
            try {
                SWIGTYPE_p_LLVMOpaqueValue initializer = bitreader.LLVMGetInitializer(global);
                if (initializer != null) {
                    Cfe cfe;
                    LValue globalToInitialize = new Indirection(valueParser.parseLValue(null, global));
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
            global = bitreader.LLVMGetNextGlobal(global);
        }

        return first;
    }

    private Cfe processBlock(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock) {
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
}
