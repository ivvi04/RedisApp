package ru.lakeevda.redisapp.domain.exception;

/**
 * Исключение при попытке создать уже существующий ресурс.
 *
 * @author lakeev-da
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
