package ru.lakeevda.redisapp.dto;

public record MessageSendRequest(Long chatId, Long senderId, String content) {
}
