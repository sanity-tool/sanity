package ru.urururu.sanity.cpp;

/**
 * @author <a href="mailto:dmitriy.g.matveev@gmail.com">Dmitry Matveev</a>
 */
public class RecoverableException extends RuntimeException {
    public RecoverableException(String message) {
        super(message);
    }

    public RecoverableException(Throwable cause) {
        super(cause);
    }
}
