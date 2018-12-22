package ru.urururu.sanity.cpp;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ParserControllerApi;
import io.swagger.client.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.*;

import java.io.File;
import java.io.IOException;
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

    private Cfe parseGlobalInitializers(ModuleDto module) {
        CfgBuilder builder = new CfgBuilder();

        int globalIndex = 0;
        for (ValueDto global : module.getGlobals()) {
            ValueRefDto globalRef = new ValueRefDto();
            globalRef.setIndex(globalIndex++);
            globalRef.setKind(ValueRefDto.KindEnum.GLOBAL);

            try {
                List<ValueRefDto> initializer = global.getOperands();
                if (CollectionUtils.isNotEmpty(initializer)) {
                    GlobalVar pointerToGlobal = (GlobalVar) valueParser.parseLValue(null, globalRef);

                    if (pointerToGlobal.getName().contains("rustc_debug")) {
                        continue;
                    }

                    LValue globalToInitialize = new Indirection(pointerToGlobal);
                    // todo constant struct and arrays are not yet implemented
                    builder.append(new Assignment(globalToInitialize, valueParser.parseRValue(null, initializer.get(0)), null));
                }
            } catch (Exception e) {
                System.err.println("Can't parse global: " + global.getName());
                e.printStackTrace(System.err);
            }
        }

        return builder.getResult();
    }

    @Override
    public List<Cfg> parse(File file) {
        ModuleDto m;
        try {
            ApiClient apiClient = new ApiClient();
            apiClient.setBasePath(System.getenv("BITREADER_URL"));

            ParserControllerApi parserApi = new ParserControllerApi(apiClient);
            byte[] bytes = Files.readAllBytes(file.toPath());
            m = parserApi.parseUsingPOST(bytes);
        } catch (IOException | ApiException e) {
            throw new IllegalStateException(e);
        }

        if (m == null) {
            return Collections.emptyList();
        }

        for (ParserListener listener : listeners) {
            listener.onModuleStarted(m);
        }

        try {
            ArrayList<Cfg> result = new ArrayList<>();

            for (FunctionDto function : m.getFunctions()) {
                if (!function.getBlocks().isEmpty()) {
                    RemoteCfgBuildingCtx ctx = new RemoteCfgBuildingCtx(parsers, function);

                    BlockDto entryBlock = function.getBlocks().get(function.getEntryBlockIndex());

                    Cfe entry = parsers.parseBlock(ctx, entryBlock);

                    int i = 0;
                    for (BlockDto block : function.getBlocks()) {
                        ValueRefDto valueRefDto = new ValueRefDto();
                        valueRefDto.setKind(ValueRefDto.KindEnum.BLOCK);
                        valueRefDto.setIndex(i++);

                        Cfe blockEntry = parsers.parseBlock(ctx, block);
                        Cfe label = ctx.getLabel(valueRefDto);

                        label.setNext(blockEntry);
                    }

                    entry = cfgUtils.removeNoOps(entry);

                    result.add(new Cfg((FunctionAddress) valueParser.parseRValue(null, function.getRef()), entry));
                }
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
        }
    }
}
