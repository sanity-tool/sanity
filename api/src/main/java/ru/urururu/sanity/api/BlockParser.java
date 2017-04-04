package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.api.cfg.Cfe;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class BlockParser<M, T, V, I, B, Ctx extends CfgBuildingCtx<M, T, V, I, B, Ctx>> {
    @Autowired
    protected ParsersFacade<M, T, V, I, B, Ctx> parsers;

    protected abstract Cfe processBlock(Ctx ctx, B block);

    protected Cfe processBlock(CfgBuildingCtx<M, T, V, I, B, Ctx> ctx, Iterable<I> instructions) {
        for (I instruction : instructions) {
            Cfe cfe = parsers.parse((Ctx) ctx, instruction);
            if (cfe != null) {
                ctx.append(cfe);
            }
        }

        return ctx.endSubCfg();
    }
}
