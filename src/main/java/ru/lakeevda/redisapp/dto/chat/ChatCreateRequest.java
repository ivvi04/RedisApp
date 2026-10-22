package ru.lakeevda.redisapp.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ChatCreateRequest(
        @Size(max = 255, message = "Название чата не должно превышать 255 символов")
        String name,

        @NotBlank(message = "Укажите тип чата")
        String type,

        @NotNull(message = "Укажите участников чата")
        Set<@NotNull(message = "Идентификатор участника не может быть пустым") Long> memberIds,

        @NotNull(message = "Укажите создателя чата")
        Long createdById) {
}
