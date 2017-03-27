package ru.urururu.sanity.api.cfg;

import org.springframework.stereotype.Component;
import ru.urururu.sanity.cpp.Demangler;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
@Component
public class ConstCache {
    public Const get(long value, Type type) {
        return new Const(value, type);
    }

    public RValue get(double value, Type type) {
        return new RealConst(value, type);
    }

    public RValue get(String s, Type type) {
        return new StringConst(s, type);
    }

    public RValue getNull(Type type) {
        return new NullPtr(type);
    }

    public RValue getFunction(String name, Type type) {
        name = fixName(name);
        return new FunctionAddress(name, type);
    }

    private String fixName(String name) {
        return name.replaceFirst("17([0-9a-z]{17})", "1700000000000000000");
    }
}
