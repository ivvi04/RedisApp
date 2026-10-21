package ru.lakeevda.redisapp.dto;

import java.time.Instant;

public record UserResponse(Long id, String email, String username, Instant createdAt) {
}
