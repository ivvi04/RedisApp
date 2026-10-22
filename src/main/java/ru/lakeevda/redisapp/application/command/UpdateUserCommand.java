package ru.lakeevda.redisapp.application.command;

/**
 * Команда обновления пользователя.
 *
 * @param email    email пользователя
 * @param username имя пользователя
 * @author lakeev-da
 */
public record UpdateUserCommand(String email, String username) {
}
