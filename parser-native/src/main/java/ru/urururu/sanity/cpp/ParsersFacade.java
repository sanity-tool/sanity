package ru.urururu.sanity.cpp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.cfg.CfgBuildingCtx;
import ru.urururu.sanity.api.cfg.RValue;
import ru.urururu.sanity.api.cfg.SourceRange;
import ru.urururu.sanity.api.cfg.Type;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueType;
import ru.urururu.sanity.cpp.llvm.SWIGTYPE_p_LLVMOpaqueValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ParsersFacade {
    @Autowired
    ValueParser valueParser;

    @Autowired
    NativeTypeParser typeParser;

    @Autowired
    NativeSourceRangeFactory sourceRangeFactory;

    public Type parse(SWIGTYPE_p_LLVMOpaqueType type) {
        return typeParser.parse(type);
    }

    public RValue parseRValue(CfgBuildingCtx ctx, SWIGTYPE_p_LLVMOpaqueValue value) {
        return valueParser.parseRValue(ctx, value);
    }

    public SourceRange getSourceRange(SWIGTYPE_p_LLVMOpaqueValue instruction) {
        return sourceRangeFactory.getSourceRange(instruction);
    }
}
