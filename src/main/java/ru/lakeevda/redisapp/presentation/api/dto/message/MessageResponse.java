package ru.lakeevda.redisapp.presentation.api.dto.message;

import java.time.Instant;

/**
 * Данные сообщения для API.
 *
 * @param id        идентификатор сообщения
 * @param senderId  идентификатор отправителя
 * @param chatId    идентификатор чата
 * @param content   текст сообщения
 * @param timestamp время отправки
 * @param status    статус сообщения
 * @author lakeev-da
 */
public record MessageResponse(Long id, Long senderId, Long chatId, String content, Instant timestamp, String status) {
}
