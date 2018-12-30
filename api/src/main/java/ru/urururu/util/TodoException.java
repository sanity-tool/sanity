package ru.urururu.util;

public class TodoException extends RuntimeException {
    private TodoException(String message) {
        super(message);
    }

    public static <T> T todo() {
        return todo("");
    }

    public static <T> T todo(String message) {
        throw new TodoException(message);
    }
}
