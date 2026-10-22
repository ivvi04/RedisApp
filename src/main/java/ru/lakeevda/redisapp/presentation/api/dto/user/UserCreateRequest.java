package ru.lakeevda.redisapp.presentation.api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание пользователя.
 *
 * @param email    email пользователя
 * @param username имя пользователя
 * @param password пароль
 * @author lakeev-da
 */
public record UserCreateRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Укажите имя пользователя")
        @Size(max = 255, message = "Имя пользователя слишком длинное")
        String username,

        @NotBlank(message = "Укажите пароль")
        @Size(max = 255, message = "Пароль слишком длинный")
        String password) {
}
