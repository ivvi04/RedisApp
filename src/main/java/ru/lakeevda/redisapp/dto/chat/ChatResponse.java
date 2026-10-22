package ru.lakeevda.redisapp.dto.chat;

import java.time.Instant;
import java.util.Set;

/**
 * Данные чата для API.
 *
 * @param id        идентификатор чата
 * @param name      название чата
 * @param type      тип чата
 * @param memberIds идентификаторы участников
 * @param createdAt дата создания
 * @author lakeev-da
 */
public record ChatResponse(Long id, String name, String type, Set<Long> memberIds, Instant createdAt) {
}
