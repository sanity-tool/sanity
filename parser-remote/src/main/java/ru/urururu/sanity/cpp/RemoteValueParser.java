package ru.urururu.sanity.cpp;

import io.swagger.client.model.BlockDto;
import io.swagger.client.model.InstructionDto;
import io.swagger.client.model.ValueRefDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.ConstCache;
import ru.urururu.sanity.api.cfg.RValue;

import java.util.function.Function;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class RemoteValueParser extends ValueParser<Integer,
        ValueRefDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> {
    @Autowired
    GlobalVariableCache globals;
    @Autowired
    ConstCache constants;
    @Autowired
    RemoteParsersFacade parsers;

    public RValue parseLValue(RemoteCfgBuildingCtx ctx, ValueRefDto value) {
        throw new IllegalStateException("Can't parse LValue: " + value);
    }

    public RValue parseRValue(RemoteCfgBuildingCtx ctx, ValueRefDto value) {
        return parseLValue(ctx, value);
    }

    private void check(ValueRefDto value, Function<ValueRefDto, ValueRefDto> test, String err) {
        if (test.apply(value) != null) {
            throw new IllegalStateException(err);
        }
    }
}
