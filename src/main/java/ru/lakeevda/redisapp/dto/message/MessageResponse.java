package ru.lakeevda.redisapp.dto.message;

import java.time.Instant;

public record MessageResponse(Long id, Long senderId, Long chatId, String content, Instant timestamp, String status) {
}
