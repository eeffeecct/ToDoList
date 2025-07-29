package org.example.exception;

public class DaoException extends RuntimeException {

    // Конструктор с сообщением об ошибке
    public DaoException(String message) {
        super(message);
    }

    // Конструктор с сообщением и причиной (исключением-источником)
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    // Конструктор только с причиной (исключением-источником)
    public DaoException(Throwable cause) {
        super(cause);
    }
}