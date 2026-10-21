package ru.lakeevda.redisapp.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
        @NotNull Long chatId,
        @NotNull Long senderId,
        @NotBlank @Size(max = 1000) String content) {
}
