package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.*;
import ru.urururu.util.FinalMap;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public abstract class CfgBuildingCtx<M, T, V, I, B, Ctx/*todo?*/ extends CfgBuildingCtx<M, T, V, I, B, Ctx>> {
    protected final ParsersFacade<M, T, V, I, B, Ctx> parsers;

    protected Map<V, RValue> params = FinalMap.createHashMap();
    private Map<I, LValue> tmpVars = FinalMap.createHashMap();
    protected Map<B, Cfe> labels = FinalMap.createHashMap();
    protected B block;
    protected CfgBuilder builder;

    protected CfgBuildingCtx(ParsersFacade<M, T, V, I, B, Ctx> parsers) {
        this.parsers = parsers;
    }

    protected LValue getOrCreateTmpVar(I instruction, T type) {
        return tmpVars.computeIfAbsent(instruction, k -> new TemporaryVar(parsers.parse(type)));
    }

    public abstract LValue getOrCreateTmpVar(I instruction);

    public final Cfe getBlockEntrance(B block) {
        return labels.computeIfAbsent(block, k -> new NoOp(null));
    }

    public abstract Cfe getLabel(V label); // todo change to B block?

    protected final Cfe prependPhiAssignment(I phi, Cfe blockEntrance, Iterable<B> incomingBlocks, Iterable<V> incomingValues) {
        Iterator<B> blocksIterator = incomingBlocks.iterator();
        Iterator<V> valuesIterator = incomingValues.iterator();

        while (blocksIterator.hasNext()) {
            B incomingBlock = blocksIterator.next();
            V incomingValue = valuesIterator.next();

            if (this.block.equals(incomingBlock)) {
                Assignment phiAssignment = new Assignment(
                        getOrCreateTmpVar(phi),
                        parsers.parseRValue((Ctx) this, incomingValue),
                        parsers.getSourceRange(phi)
                );
                phiAssignment.setNext(blockEntrance);

                return phiAssignment;
            }

        }

        if (valuesIterator.hasNext()) {
            throw new IllegalStateException("Out of blocks, but have more values");
        }

        Stream<B> stream = StreamSupport.stream(incomingBlocks.spliterator(), false);
        throw new IllegalStateException("Can't match incoming block: " + blockToString(this.block) + " to one of "
                + stream.map(this::blockToString).collect(Collectors.joining()));
    }

    protected abstract String blockToString(B block);

    public Cfe endSubCfg() {
        try {
            return builder.getResult();
        } finally {
            builder = null;
        }
    }

    public void append(Cfe cfe) {
        builder.append(cfe);
    }

    public RValue getParam(V value) {
        return params.get(value);
    }

    public LValue getTmpVar(I instruction) {
        LValue result = tmpVars.get(instruction);
        if (result == null) {
            throw new IllegalStateException("not created yet");
        }
        return result;
    }

    public void beginSubCfg(B entryBlock) {
        block = entryBlock;
        builder = new CfgBuilder();
    }
}
