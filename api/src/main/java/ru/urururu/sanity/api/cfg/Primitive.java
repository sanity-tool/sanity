package ru.urururu.sanity.api.cfg;

/**
* @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
*/
public class Primitive implements Type {
    private String name;

    public Primitive(String name) {
        this.name = name;
    }

    @Override
    public Type getElementType() {
        return null;
    }

    @Override
    public Type getFieldType(int index) {
        return null;
    }

    @Override
    public String getFieldName(int index) {
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
