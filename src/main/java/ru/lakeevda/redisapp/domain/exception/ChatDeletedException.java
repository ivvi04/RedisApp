package ru.lakeevda.redisapp.domain.exception;

/**
 * Исключение при обращении к удалённому чату.
 *
 * @author lakeev-da
 */
public class ChatDeletedException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ChatDeletedException(String message) {
        super(message);
    }
}
