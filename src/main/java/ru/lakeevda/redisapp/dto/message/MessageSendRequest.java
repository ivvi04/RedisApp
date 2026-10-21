package ru.lakeevda.redisapp.dto.message;

public record MessageSendRequest(Long chatId, Long senderId, String content) {
}
