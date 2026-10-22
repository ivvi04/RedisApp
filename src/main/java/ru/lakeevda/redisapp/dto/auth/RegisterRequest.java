package ru.lakeevda.redisapp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        @Size(max = 255, message = "Email слишком длинный")
        String email,

        @NotBlank(message = "Укажите имя пользователя")
        @Size(max = 255, message = "Имя пользователя слишком длинное")
        String username,

        @NotBlank(message = "Укажите пароль")
        @Size(min = 6, max = 255, message = "Пароль должен содержать от 6 до 255 символов")
        String password,

        @NotBlank(message = "Повторите пароль")
        String confirmPassword) {
}
