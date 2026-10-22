package ru.lakeevda.redisapp.domain.exception;

/**
 * Исключение при попытке выполнить действие с чужим сообщением.
 *
 * @author lakeev-da
 */
public class MessageAccessDeniedException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public MessageAccessDeniedException(String message) {
        super(message);
    }
}
