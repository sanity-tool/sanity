package na.okutane.cpp;

import na.okutane.CfgUtils;
import na.okutane.api.Cfg;
import na.okutane.api.cfg.Cfe;
import na.okutane.api.cfg.CfgBuildingCtx;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueModule;
import na.okutane.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import na.okutane.cpp.llvm.bitreader;
import na.okutane.utils.TempFileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitriy Matveev</a>
 */
@Component
public class Parser {
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    InstructionParser instructionParser;
    @Autowired
    TypeParser typeParser;
    @Autowired
    ParserListener[] listeners;

    public List<Cfg> parse(String filename) throws ParseException {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.directory(new File("/Users/jondoe/IdeaProjects/SA/sanity/tests"));
            //pb.command("clang", filename, "-c", "-S", "-emit-llvm", "-gline-tables-only");

            try (TempFileWrapper objFile = new TempFileWrapper("result", ".bc")) {
                try (TempFileWrapper errFile = new TempFileWrapper("result", ".err")) {
                    pb.command(getClangParameters(filename, objFile.getAbsolutePath()));

                    pb.inheritIO();
                    pb.redirectError(ProcessBuilder.Redirect.to(errFile.getFile()));

                    Process process = pb.start();

                    int resultCode = process.waitFor();

                    if (resultCode == 0) {

                        SWIGTYPE_p_LLVMOpaqueModule m = bitreader.parse(objFile.getAbsolutePath());

                        if (m == null) {
                            return Collections.emptyList();
                        }

                        bitreader.LLVMDumpModule(m); // todo move to separate listener for test/debug
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
                                    System.err.println("Can't parse function");
                                    e.printStackTrace(System.err);
                                }
                                function = bitreader.LLVMGetNextFunction(function);
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

    private String[] getClangParameters(String filename, String objFile) {
        List<String> parameters = new ArrayList<>();

        if (filename.endsWith(".c")) {
            parameters.add("clang");
        } else {
            parameters.add("clang++");
        }

        parameters.addAll(Arrays.asList(filename, "-c", "-emit-llvm", "-femit-all-decls", "-g", "-o", objFile));

        return parameters.toArray(new String[parameters.size()]);
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
