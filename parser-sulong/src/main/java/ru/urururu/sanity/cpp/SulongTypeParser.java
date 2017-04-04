package ru.urururu.sanity.cpp;

import com.oracle.truffle.llvm.runtime.types.*;
import com.oracle.truffle.llvm.runtime.types.ArrayType;
import com.oracle.truffle.llvm.runtime.types.PointerType;
import com.oracle.truffle.llvm.runtime.types.Type;
import com.oracle.truffle.llvm.runtime.types.metadata.MetadataConstantPointerType;
import com.oracle.truffle.llvm.runtime.types.metadata.MetadataConstantType;
import com.oracle.truffle.llvm.runtime.types.visitors.TypeVisitor;
import org.springframework.stereotype.Component;
import ru.urururu.util.FinalReference;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class SulongTypeParser extends TypeParser<Type> {
    @Override
    protected ru.urururu.sanity.api.cfg.Type doParse(Type type) {
        FinalReference<ru.urururu.sanity.api.cfg.Type> result = new FinalReference<>("Type");

        type.accept(new TypeVisitor() {
            @Override
            public void visit(BigIntegerConstantType bigIntegerConstantType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(FloatingPointType floatingPointType) {
                result.set(createFloat());
            }

            @Override
            public void visit(FunctionType functionType) {
                result.set(createFunction(functionType.getReturnType(), functionType.getArgumentTypes()));
            }

            @Override
            public void visit(IntegerConstantType integerConstantType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(IntegerType integerType) {
                result.set(createInt(integerType.getBits()));
            }

            @Override
            public void visit(MetadataConstantType metadataConstantType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(MetadataConstantPointerType metadataConstantPointerType) {
                throw new NotImplementedException();
            }

            @Override
            public void visit(MetaType metaType) {
                if (metaType == MetaType.VOID) {
                    result.set(createVoid());
                    return;
                }

                if (metaType == MetaType.OPAQUE) {
                    result.set(createStructure(type, "", new Type[0]));
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
                result.set(createArray(arrayType.getLength(), arrayType.getElementType()));
            }

            @Override
            public void visit(StructureType structureType) {
                Type[] fieldTypes = new Type[structureType.getLength()];
                for (int i = 0; i < fieldTypes.length; i++) {
                    fieldTypes[i] = structureType.getElementType(i);
                }

                result.set(createStructure(structureType, structureType.getName(), fieldTypes));
            }

            @Override
            public void visit(VectorType vectorType) {
                throw new NotImplementedException();
            }
        });

        return result.get();
    }
}
