package ru.urururu.sanity.cpp;

import io.swagger.client.model.*;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.*;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RemoteCfgBuildingCtx extends CfgBuildingCtx<Integer,
        ValueRefDto, InstructionDto, BlockDto, RemoteCfgBuildingCtx> {
    private final FunctionDto function;

    public RemoteCfgBuildingCtx(RemoteParsersFacade parsers, FunctionDto function) {
        super(parsers);
        this.function = function;

        int i = 0;
        for (ValueDto param : function.getParams()) {
            ValueRefDto paramRef = new ValueRefDto();
            paramRef.setKind(ValueRefDto.KindEnum.ARGUMENT);
            paramRef.setIndex(i);

            params.put(paramRef, new Parameter(params.size(), param.getName(), parsers.parse(param.getTypeId())));
            i++;
        }
    }

    public LValue getOrCreateTmpVar(InstructionDto instruction) {
        return getOrCreateTmpVar(instruction, instruction.getTypeId());
    }

    public RValue getParam(ValueRefDto value) {
        return params.get(value);
    }

    public Cfe getLabel(ValueRefDto label) {
        if (label.getKind() != ValueRefDto.KindEnum.BLOCK) {
            throw new IllegalArgumentException(label.toString());
        }

        Integer blockId = label.getIndex();
        BlockDto block = function.getBlocks().get(blockId);

        Cfe result = labels.computeIfAbsent(block, k -> new NoOp(null));

        InstructionDto instruction = block.getInstructions().iterator().next();
        if (instruction.getKind().equals("LLVMPHI")) {
            long n = instruction.getIncomingBlocks().size();
            for (int i = 0; i < n; i++) {
                if (this.block.equals(function.getBlocks().get(instruction.getIncomingBlocks().get(i).getIndex()))) {
                    Assignment phiAssignment = new Assignment(
                            getOrCreateTmpVar(instruction),
                            parsers.parseRValue(this, instruction.getIncomingValues().get(i)),
                            parsers.getSourceRange(instruction)
                    );
                    phiAssignment.setNext(result);

                    return phiAssignment;
                }
            }
        }

        return result;
    }
}
