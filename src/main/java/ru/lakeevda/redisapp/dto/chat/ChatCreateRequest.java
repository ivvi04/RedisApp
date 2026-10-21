package ru.lakeevda.redisapp.dto.chat;

import java.util.Set;

public record ChatCreateRequest(String name, String type, Set<Long> memberIds, Long createdById) {
}
