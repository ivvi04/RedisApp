package ru.lakeevda.redisapp.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotNull(message = "Укажите идентификатор чата")
        Long chatId,

        @NotNull(message = "Укажите идентификатор отправителя")
        Long senderId,

        @NotBlank(message = "Введите текст сообщения")
        @Size(max = 1000, message = "Сообщение не должно превышать 1000 символов")
        String content) {
}
