package ru.urururu.sanity.api;

import ru.urururu.sanity.api.cfg.Cfe;
import ru.urururu.sanity.api.cfg.RValue;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public interface Violation {
    RValue getValue();

    Cfe getPoint();
}
