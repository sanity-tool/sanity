package ru.urururu.sanity.api.cfg;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class CfgBuilder {
    private Cfe first = null;
    private Cfe last = null;

    public void append(Cfe cfe) {
        if (first == null) {
            first = last = cfe;
        } else {
            last.setNext(cfe);
            last = cfe;
        }
    }

    public Cfe getResult() {
        return first;
    }
}
