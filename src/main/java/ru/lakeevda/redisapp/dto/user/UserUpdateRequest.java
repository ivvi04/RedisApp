package ru.lakeevda.redisapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Укажите email")
        @Email(message = "Некорректный email")
        String email,

        @NotBlank(message = "Укажите имя пользователя")
        @Size(max = 255, message = "Имя пользователя слишком длинное")
        String username) {
}
