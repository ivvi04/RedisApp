package ru.lakeevda.redisapp.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Запрос на создание чата.
 *
 * @param name        название чата
 * @param memberIds   идентификаторы участников
 * @param createdById идентификатор создателя
 * @author lakeev-da
 */
public record ChatCreateRequest(
        @Size(max = 255, message = "Название чата не должно превышать 255 символов")
        String name,

        @NotNull(message = "Укажите участников чата")
        Set<@NotNull(message = "Идентификатор участника не может быть пустым") Long> memberIds,

        @NotNull(message = "Укажите создателя чата")
        Long createdById) {
}
