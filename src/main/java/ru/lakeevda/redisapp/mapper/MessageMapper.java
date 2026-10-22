package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.entity.MessageEntity;

/**
 * Преобразование сущности сообщения в DTO.
 *
 * @author lakeev-da
 */
public class MessageMapper {

    /**
     * Преобразует сущность сообщения в DTO.
     *
     * @param entity сущность сообщения
     * @return DTO сообщения
     */
    public static MessageResponse fromEntity(MessageEntity entity) {
        return new MessageResponse(
                entity.getId(),
                entity.getSenderId(),
                entity.getChatId(),
                entity.getContent(),
                entity.getTimestamp(),
                entity.getStatus());
    }
}
