package ru.urururu.llvm.bitreader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Function extends Value {
    private Function next;
    private List<Instruction> instructions;
    private List<Value> arguments;

    Function(Type type) {
        super(type);
    }

    void setNext(Function next) {
        this.next = next;
    }

    public Function getNext() {
        return next;
    }

    void setInstructions(List<Instruction> instructions) {
        this.instructions = instructions;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public List<Value> getArguments() {
        if (arguments == null) {
            Argument lastArg = null;

            FunctionType type = (FunctionType) ((PointerType) getType()).getElementType();
            arguments = new ArrayList<>();
            for (Type argType : type.getParamTypes()) {
                Argument arg = new Argument(argType);
                arguments.add(arg);

                if (lastArg != null) {
                    lastArg.setNext(arg);
                }

                lastArg = arg;
            }
        }

        return arguments;
    }

    public List<Block> getBlocks() {
        return Collections.singletonList(new Block(instructions));
    }
}
