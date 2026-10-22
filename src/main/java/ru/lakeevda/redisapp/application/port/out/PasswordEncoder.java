package ru.lakeevda.redisapp.application.port.out;

/**
 * Исходящий порт хеширования паролей.
 *
 * @author lakeev-da
 */
public interface PasswordEncoder {

    /**
     * Хеширует пароль.
     *
     * @param rawPassword исходный пароль
     * @return хеш пароля
     */
    String encode(String rawPassword);
}
