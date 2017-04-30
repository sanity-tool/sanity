package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.parser.model.ModelModule;
import com.oracle.truffle.llvm.runtime.types.*;
import com.oracle.truffle.llvm.runtime.types.ArrayType;
import com.oracle.truffle.llvm.runtime.types.PointerType;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.visitors.TypeVisitor;
import org.springframework.stereotype.Component;
import ru.urururu.sanity.api.TypeParser;
import ru.urururu.util.FinalReference;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongTypeParser extends TypeParser<ModelModule, Type> {
    @Override
    public ru.urururu.sanity.api.cfg.Type parse(Type type) {
        FinalReference<ru.urururu.sanity.api.cfg.Type> result = new FinalReference<>("Type");

        type.accept(new TypeVisitor() {
            @Override
            public void visit(FunctionType functionType) {
                result.set(createFunction(functionType.getReturnType(), Arrays.asList(functionType.getArgumentTypes())));
            }

            @Override
            public void visit(PrimitiveType primitiveType) {
                switch (primitiveType.getPrimitiveKind()) {
                    case I1:
                    case I8:
                    case I16:
                    case I32:
                    case I64:
                        result.set(createInt(primitiveType.getBitSize()));
                        return;
                    case HALF:
                        break;
                    case FLOAT:
                        result.set(createFloat());
                        return;
                    case DOUBLE:
                        result.set(createDouble());
                        return;
                    case F128:
                        break;
                    case X86_FP80:
                        result.set(createLongDouble());
                        return;
                    case PPC_FP128:
                        break;
                }
                throw new IllegalStateException(primitiveType.getPrimitiveKind().name());
            }

            @Override
            public void visit(MetaType metaType) {
                if (metaType == MetaType.OPAQUE) {
                    result.set(createStruct(type, "", Collections.emptyList()));
                    return; // is this correct?
                }

                throw new NotImplementedException();
            }

            @Override
            public void visit(PointerType pointerType) {
                result.set(createPointer(pointerType.getPointeeType()));
            }

            @Override
            public void visit(ArrayType arrayType) {
                result.set(createArray(arrayType.getElementType(), arrayType.getNumberOfElements()));
            }

            @Override
            public void visit(StructureType structureType) {
                Type[] fieldTypes = new Type[structureType.getNumberOfElements()];
                for (int i = 0; i < fieldTypes.length; i++) {
                    fieldTypes[i] = structureType.getElementType(i);
                }

                result.set(createStruct(structureType, structureType.getName(), Arrays.asList(fieldTypes)));
            }

            @Override
            public void visit(VectorType vectorType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(VariableBitWidthType vectorType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(VoidType vectorType) {
                result.set(createVoid());
            }
        });

        return result.get();
    }
}
