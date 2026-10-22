package ru.lakeevda.redisapp.exception;

/**
 * Исключение при пустом аргументе запроса.
 *
 * @author lakeev-da
 */
public class IllegalArgumentRequestException extends IllegalArgumentException {
    private static final String MESSAGE = "Аргумент запроса не может быть пустым";

    /**
     * Создаёт исключение с сообщением по умолчанию.
     */
    public IllegalArgumentRequestException() {
        super(MESSAGE);
    }
}
