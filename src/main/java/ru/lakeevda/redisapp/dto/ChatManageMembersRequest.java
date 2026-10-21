package ru.lakeevda.redisapp.dto;

public record ChatManageMembersRequest(Long chatId, Long userId, String action) {
}
