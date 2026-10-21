package ru.lakeevda.redisapp.dto.user;

public record UserCreateRequest(String email, String username, String password) {
}
