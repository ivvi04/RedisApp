package ru.lakeevda.redisapp.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ChatCreateRequest(
        @Size(max = 255) String name,
        @NotBlank String type,
        @NotNull Set<@NotNull Long> memberIds,
        @NotNull Long createdById) {
}
