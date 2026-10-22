package ru.lakeevda.redisapp.application.exception;

/**
 * Исключение при некорректном аргументе запроса.
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
