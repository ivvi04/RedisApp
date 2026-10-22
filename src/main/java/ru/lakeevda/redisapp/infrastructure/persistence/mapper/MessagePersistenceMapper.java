package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.MessageEntity;

/**
 * Преобразование JPA-сущности сообщения и доменной модели.
 *
 * @author lakeev-da
 */
@Component
public class MessagePersistenceMapper {

    /**
     * Преобразует JPA-сущность в доменную модель.
     *
     * @param entity JPA-сущность
     * @return доменная модель сообщения
     */
    public Message toDomain(MessageEntity entity) {
        return new Message(
                entity.getId(),
                entity.getSenderId(),
                entity.getChatId(),
                entity.getContent(),
                entity.getTimestamp(),
                MessageStatus.fromValue(entity.getStatus()));
    }

    /**
     * Переносит данные доменной модели в JPA-сущность.
     *
     * @param message доменная модель
     * @param entity  JPA-сущность
     */
    public void apply(Message message, MessageEntity entity) {
        entity.setId(message.id());
        entity.setSenderId(message.senderId());
        entity.setChatId(message.chatId());
        entity.setContent(message.content());
        entity.setTimestamp(message.timestamp());
        entity.setStatus(message.status().getValue());
    }
}
