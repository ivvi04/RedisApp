package ru.lakeevda.redisapp.domain.exception;

/**
 * Исключение при отсутствии запрошенного ресурса.
 *
 * @author lakeev-da
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с указанным сообщением.
     *
     * @param message сообщение об ошибке
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
