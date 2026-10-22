package ru.lakeevda.redisapp.presentation.api.mapper;

import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserResponse;

/**
 * Преобразование доменной модели пользователя в DTO.
 *
 * @author lakeev-da
 */
public final class UserResponseMapper {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private UserResponseMapper() {
    }

    /**
     * Преобразует доменную модель пользователя в DTO.
     *
     * @param user доменная модель
     * @return DTO пользователя
     */
    public static UserResponse fromDomain(User user) {
        return new UserResponse(user.id(), user.email(), user.username(), user.createdAt());
    }
}
