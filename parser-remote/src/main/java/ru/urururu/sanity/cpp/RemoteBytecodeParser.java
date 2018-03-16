package ru.urururu.sanity.cpp;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ParserControllerApi;
import io.swagger.client.model.BlockDto;
import io.swagger.client.model.FunctionDto;
import io.swagger.client.model.ModuleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.ConstCache;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteBytecodeParser implements BytecodeParser {
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    RemoteParsersFacade parsers;
    @Autowired
    RemoteTypeParser typeParser;
    @Autowired
    RemoteValueParser valueParser;
    @Autowired
    ParserListener[] listeners;
    @Autowired
    ConstCache constants;

    @Override
    public List<Cfg> parse(File file) {
        ModuleDto m;
        try {
            ParserControllerApi parserApi = new ParserControllerApi(new ApiClient());
            byte[] bytes = Files.readAllBytes(file.toPath());
            m = parserApi.parseUsingPOST(bytes);
        } catch (IOException | ApiException e) {
            throw new IllegalStateException(e);
        }

        if (m == null) {
            return Collections.emptyList();
        }

        ArrayList<Cfg> result = new ArrayList<>();

        for (FunctionDto function : m.getFunctions()) {
            if (bitreader.LLVMGetFirstBasicBlock(function) != null) {
                RemoteCfgBuildingCtx ctx = new RemoteCfgBuildingCtx(parsers, function);

                SWIGTYPE_p_LLVMOpaqueBasicBlock entryBlock = bitreader.LLVMGetEntryBasicBlock(function);

                Cfe entry = parsers.parseBlock(ctx, entryBlock);

                SWIGTYPE_p_LLVMOpaqueBasicBlock block = bitreader.LLVMGetFirstBasicBlock(function);
                block = bitreader.LLVMGetNextBasicBlock(block);

                for (BlockDto block : function.getBlocks()) {

                }

                while (block != null) {
                    Cfe blockEntry = parsers.parseBlock(ctx, block);
                    Cfe label = ctx.getLabel(bitreader.LLVMBasicBlockAsValue(block));

                    label.setNext(blockEntry);

                    block = bitreader.LLVMGetNextBasicBlock(block);
                }

                entry = cfgUtils.removeNoOps(entry);

                result.add(new Cfg((FunctionAddress) valueParser.parseRValue(null, function), entry));
            }
        }

        return result;
    }
}
