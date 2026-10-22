package ru.lakeevda.redisapp.presentation.api.mapper;

import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.presentation.api.dto.message.MessageResponse;

/**
 * Преобразование доменной модели сообщения в DTO.
 *
 * @author lakeev-da
 */
public final class MessageResponseMapper {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private MessageResponseMapper() {
    }

    /**
     * Преобразует доменную модель сообщения в DTO.
     *
     * @param message доменная модель
     * @return DTO сообщения
     */
    public static MessageResponse fromDomain(Message message) {
        return new MessageResponse(
                message.id(),
                message.senderId(),
                message.chatId(),
                message.content(),
                message.timestamp(),
                message.status().getValue());
    }
}
