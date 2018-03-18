package ru.urururu.sanity.cpp;

import io.swagger.client.model.*;
import ru.urururu.sanity.api.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.*;
import ru.urururu.sanity.cpp.NativeParsersFacade;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueBasicBlock;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;
import ru.urururu.sanity.cpp.llvm.bitreader;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RemoteCfgBuildingCtx extends CfgBuildingCtx<Integer,
        ValueRefDto, ValueRefDto, BlockDto, RemoteCfgBuildingCtx> {

    public RemoteCfgBuildingCtx(RemoteParsersFacade parsers, FunctionDto function) {
        super(parsers);

        int i = 0;
        for (ValueDto param : function.getParams()) {
            ValueRefDto paramREf = new ValueRefDto();
            paramREf.setKind(ValueRefDto.KindEnum.ARGUMENT);
            paramREf.setIndex(i);

            params.put(paramREf, new Parameter(params.size(), param.getName(), parsers.parse(param.getTypeId())));
            i++;
        }
    }

    public LValue getOrCreateTmpVar(ValueRefDto instruction) {
        return getOrCreateTmpVar(instruction, 0/*todo*/);
    }

    public RValue getParam(ValueRefDto value) {
        return params.get(value);
    }

    public Cfe getLabel(ValueRefDto label) {
        if (label.getKind() != ValueRefDto.KindEnum.BLOCK) {
            throw new IllegalArgumentException(label.toString());
        }

        Integer blockId = label.getIndex();
        //BlockDto block = bitreader.LLVMValueAsBasicBlock(label);

        Cfe result = labels.computeIfAbsent(block, k -> new NoOp(null));

        InstructionDto instruction = block.getInstructions().iterator().next();
//        if (instruction.getKind().equals("Phi")) { // todo
//            long n = bitreader.LLVMCountIncoming(instruction);
//            for (int i = 0; i < n; i++) {
//                if (this.block.equals(bitreader.LLVMGetIncomingBlock(instruction, i))) {
//                    Assignment phiAssignment = new Assignment(
//                            getOrCreateTmpVar(instruction),
//                            parsers.parseRValue(this, bitreader.LLVMGetIncomingValue(instruction, i)),
//                            parsers.getSourceRange(instruction)
//                    );
//                    phiAssignment.setNext(result);
//
//                    return phiAssignment;
//                }
//            }
//        }

        return result;
    }
}
