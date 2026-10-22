package ru.lakeevda.redisapp.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ChatManageMembersRequest(
        @NotNull(message = "Укажите идентификатор чата")
        Long chatId,

        @NotNull(message = "Укажите идентификатор пользователя")
        Long userId,

        @NotBlank(message = "Укажите действие")
        @Pattern(regexp = "ADD|REMOVE", message = "Действие должно быть ADD или REMOVE")
        String action) {
}
