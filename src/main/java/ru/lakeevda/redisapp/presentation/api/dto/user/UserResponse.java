package ru.lakeevda.redisapp.presentation.api.dto.user;

import java.time.Instant;

/**
 * Данные пользователя для API.
 *
 * @param id        идентификатор пользователя
 * @param email     email пользователя
 * @param username  имя пользователя
 * @param createdAt дата создания
 * @author lakeev-da
 */
public record UserResponse(Long id, String email, String username, Instant createdAt) {
}
