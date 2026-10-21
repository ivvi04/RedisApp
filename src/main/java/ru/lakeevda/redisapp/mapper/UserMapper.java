package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.entity.UserEntity;

public class UserMapper {
    public static UserEntity toEntity(UserCreateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }

    public static UserResponse fromEntity(UserEntity entity) {
        return new UserResponse(entity.getId(), entity.getEmail(), entity.getUsername(), entity.getCreatedAt());
    }

}
