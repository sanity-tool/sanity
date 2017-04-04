package ru.urururu.sanity.llvm;

import org.springframework.stereotype.Component;
import ru.urururu.llvm.bitreader.*;
import ru.urururu.sanity.api.ParsersFacade;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class PureParsersFacade extends ParsersFacade<Module, Type, Value, Instruction, Block, PureCfgBuildingCtx> {
}
