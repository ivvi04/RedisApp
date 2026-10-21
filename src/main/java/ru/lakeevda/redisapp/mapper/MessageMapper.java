package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.entity.MessageEntity;

public class MessageMapper {

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
