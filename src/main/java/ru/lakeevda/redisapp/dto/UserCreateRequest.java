package ru.lakeevda.redisapp.dto;

public record UserCreateRequest(String email, String username, String password) {
}
