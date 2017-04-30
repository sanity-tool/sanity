package ru.urururu.sanity.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.urururu.sanity.CfgUtils;
import ru.urururu.sanity.api.cfg.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class AbstractBytecodeParser<M, T, V, I, B, Ctx extends CfgBuildingCtx<M, T, V, I, B, Ctx>> implements BytecodeParser {
    @Autowired
    CfgUtils cfgUtils;
    @Autowired
    protected
    ParsersFacade<M, T, V, I, B, Ctx> parsers;
    @Autowired
    ParserListener<M>[] listeners;
    @Autowired
    protected
    ConstCache constants;

    protected abstract Iterable<? extends V> getGlobals(M module);

    private Cfe parseGlobalInitializers(M module) {
        CfgBuilder builder = new CfgBuilder();

        for (V global : getGlobals(module)) {
            try {
                V initializer = getInitializer(global);
                if (initializer != null) {
                    GlobalVar pointerToGlobal = (GlobalVar) parsers.parseLValue(null, global);

                    if (pointerToGlobal.getName().contains("rustc_debug")) {
                        continue;
                    }

                    LValue globalToInitialize = new Indirection(pointerToGlobal);
                    parseGlobalInitializer(builder, initializer, globalToInitialize);
                }
            } catch (Exception e) {
                System.err.println("Can't parse global: " + toDebugString(global));
                e.printStackTrace(System.err);
            }
        }

        return builder.getResult();
    }

    protected abstract void parseGlobalInitializer(CfgBuilder builder, V initializer, LValue globalToInitialize);

    protected void addSimpleInitializer(CfgBuilder builder, V initializer, LValue globalToInitialize) {
        builder.append(new Assignment(globalToInitialize, parsers.parseRValue(null, initializer), null));
    }

    protected abstract V getInitializer(V global);

    protected abstract M parseModule(String absolute) throws IOException;

    protected abstract Iterable<? extends V> getFunctions(M module);

    @Override
    public List<Cfg> parse(File file) {
        M m;
        try {
            m = parseModule(file.getAbsolutePath());
        } catch (IOException e) {
            return Collections.emptyList();
        }

        for (ParserListener<M> listener : listeners) {
            listener.onModuleStarted(m);
        }

        try {
            ArrayList<Cfg> result = new ArrayList<>();

            for (V function : getFunctions(m)) {
                try {
                    Iterator<B> blocks = getBlocks(function).iterator();
                    if (blocks.hasNext()) {
                        Ctx ctx = createCtx(function);

                        B entryBlock = blocks.next();

                        Cfe entry = parsers.parseBlock(ctx, entryBlock);

                        while (blocks.hasNext()) {
                            B block = blocks.next();
                            Cfe blockEntry = parsers.parseBlock(ctx, block);
                            Cfe label = ctx.getBlockEntrance(block);

                            label.setNext(blockEntry);
                        }

                        entry = cfgUtils.removeNoOps(entry);

                        result.add(new Cfg((FunctionAddress) parsers.parseRValue(null, function), entry));
                    }
                } catch (Exception e) {
                    System.err.println("Can't parse function: " + toDebugString(function));
                    e.printStackTrace(System.err);
                }
            }

            Cfe entry = parseGlobalInitializers(m);
            if (entry != null) {
                result.add(new Cfg("<module init>", entry));
            }

            return result;
        } finally {
            for (ParserListener<M> listener : listeners) {
                listener.onModuleFinished(m);
            }
            releaseModule(m);
        }
    }

    protected abstract Iterable<B> getBlocks(V function);

    protected abstract String toDebugString(V value);

    protected abstract V toValue(B block);

    protected abstract Ctx createCtx(V function);

    protected void releaseModule(M module) {

    }
}
