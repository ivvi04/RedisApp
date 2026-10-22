package ru.lakeevda.redisapp.application.command;

/**
 * Команда создания пользователя.
 *
 * @param email    email пользователя
 * @param username имя пользователя
 * @param password пароль
 * @author lakeev-da
 */
public record CreateUserCommand(String email, String username, String password) {
}
