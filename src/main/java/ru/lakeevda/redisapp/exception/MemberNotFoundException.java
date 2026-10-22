package ru.lakeevda.redisapp.exception;

/**
 * Исключение при отсутствии участника чата.
 *
 * @author lakeev-da
 */
public class MemberNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public MemberNotFoundException(String message) {
        super(message);
    }
}
