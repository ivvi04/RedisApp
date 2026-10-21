package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.UserCreateRequest;
import ru.lakeevda.redisapp.dto.UserResponse;
import ru.lakeevda.redisapp.dto.UserUpdateRequest;
import ru.lakeevda.redisapp.entity.UserEntity;

public class UserMapper {
    public static UserEntity toEntity(UserCreateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return user;
    }

    public static UserEntity toEntity(UserUpdateRequest request) {
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        return user;
    }

    public static UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), user.getCreatedAt());
    }

}
