package ru.lakeevda.redisapp.presentation.api.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Запрос на добавление или удаление участника чата.
 *
 * @param chatId идентификатор чата
 * @param userId идентификатор пользователя
 * @param action действие: {@code ADD} или {@code REMOVE}
 * @author lakeev-da
 */
public record ChatManageMembersRequest(
        @NotNull(message = "Укажите идентификатор чата")
        Long chatId,

        @NotNull(message = "Укажите идентификатор пользователя")
        Long userId,

        @NotBlank(message = "Укажите действие")
        @Pattern(regexp = "ADD|REMOVE", message = "Действие должно быть ADD или REMOVE")
        String action) {
}
