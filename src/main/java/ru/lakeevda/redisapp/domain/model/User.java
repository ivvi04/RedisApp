package ru.lakeevda.redisapp.domain.model;

import ru.lakeevda.redisapp.domain.enums.UserRole;

import java.time.Instant;

/**
 * Доменная модель пользователя.
 *
 * @param id        идентификатор пользователя
 * @param email     email пользователя
 * @param username  имя пользователя
 * @param password  хеш пароля
 * @param createdAt дата создания
 * @param role      роль пользователя
 * @author lakeev-da
 */
public record User(
        Long id,
        String email,
        String username,
        String password,
        Instant createdAt,
        UserRole role) {

    /**
     * Возвращает копию пользователя с новым email.
     *
     * @param email новый email
     * @return обновлённый пользователь
     */
    public User withEmail(String email) {
        return new User(id, email, username, password, createdAt, role);
    }

    /**
     * Возвращает копию пользователя с новым именем.
     *
     * @param username новое имя пользователя
     * @return обновлённый пользователь
     */
    public User withUsername(String username) {
        return new User(id, email, username, password, createdAt, role);
    }
}
