package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.FunctionAddress;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class Cfg {
    private final String id;
    private final Cfe entry;

    public Cfg(String id, Cfe entry) {
        this.id = id;
        this.entry = entry;
    }

    public Cfg(FunctionAddress address, Cfe entry) {
        this(address.getName(), entry);
    }

    public String getId() {
        return id;
    }

    public Cfe getEntry() {
        return entry;
    }
}
