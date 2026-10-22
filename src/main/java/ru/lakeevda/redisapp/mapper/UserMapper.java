package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.entity.UserEntity;

/**
 * Преобразование пользовательских DTO и сущностей.
 *
 * @author lakeev-da
 */
public class UserMapper {

    /**
     * Создаёт сущность пользователя из запроса.
     *
     * @param request запрос на создание пользователя
     * @return сущность пользователя
     */
    public static UserEntity toEntity(UserCreateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }

    /**
     * Преобразует сущность пользователя в DTO.
     *
     * @param entity сущность пользователя
     * @return DTO пользователя
     */
    public static UserResponse fromEntity(UserEntity entity) {
        return new UserResponse(entity.getId(), entity.getEmail(), entity.getUsername(), entity.getCreatedAt());
    }

}
