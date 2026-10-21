package ru.lakeevda.redisapp.dto.chat;

public record ChatManageMembersRequest(Long chatId, Long userId, String action) {
}
