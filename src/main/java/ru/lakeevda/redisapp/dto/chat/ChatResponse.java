package ru.lakeevda.redisapp.dto.chat;

import java.time.Instant;
import java.util.Set;

public record ChatResponse(Long id, String name, String type, Set<Long> memberIds, Instant createdAt) {
}
