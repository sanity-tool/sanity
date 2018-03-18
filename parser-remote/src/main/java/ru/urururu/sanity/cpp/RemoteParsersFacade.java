package ru.urururu.sanity.cpp;

import io.swagger.client.model.*;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.ParsersFacade;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteParsersFacade extends ParsersFacade<Integer,
        ValueRefDto, ValueRefDto, BlockDto, RemoteCfgBuildingCtx> {
}
