package ru.urururu.llvm.bitreader;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RecordReader {
    private Iterator<Object> iterator;

    public RecordReader(List<Object> record) {
        this.iterator = record.iterator();
    }

    public int nextInt() {
        Object o = iterator.next();
        if (o instanceof Iterable) {
            iterator = ((Iterable) o).iterator();
            return nextInt();
        }
        return (int) o;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public String nextString() {
        return iterator.next().toString();
    }

    public Object next() {
        return iterator.next();
    }
}
