package ru.lakeevda.redisapp.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ChatManageMembersRequest(
        @NotNull Long chatId,
        @NotNull Long userId,
        @NotBlank @Pattern(regexp = "ADD|REMOVE") String action) {
}
