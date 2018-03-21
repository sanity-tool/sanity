package ru.urururu.sanity.cpp;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.ParserControllerApi;
import io.swagger.client.model.BlockDto;
import io.swagger.client.model.FunctionDto;
import io.swagger.client.model.ModuleDto;
import io.swagger.client.model.ValueRefDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.BytecodeParser;
import ru.urururu.sanity.api.Cfg;
import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.FunctionAddress;

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

        return result;
    }
}
