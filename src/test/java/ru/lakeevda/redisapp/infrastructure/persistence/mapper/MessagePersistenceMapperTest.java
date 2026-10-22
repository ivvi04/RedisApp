package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.MessageEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты маппера сообщений.
 *
 * @author lakeev-da
 */
class MessagePersistenceMapperTest {

    private static final Instant SENT_AT = Instant.parse("2026-01-01T10:00:00Z");

    private final MessagePersistenceMapper mapper = new MessagePersistenceMapper();

    /**
     * Проверяет преобразование сущности в доменную модель.
     */
    @Test
    void toDomainMapsAllFields() {
        MessageEntity entity = new MessageEntity();
        entity.setId(3L);
        entity.setSenderId(1L);
        entity.setChatId(10L);
        entity.setContent("hello");
        entity.setTimestamp(SENT_AT);
        entity.setStatus("read");

        Message message = mapper.toDomain(entity);

        assertThat(message.id()).isEqualTo(3L);
        assertThat(message.senderId()).isEqualTo(1L);
        assertThat(message.chatId()).isEqualTo(10L);
        assertThat(message.content()).isEqualTo("hello");
        assertThat(message.timestamp()).isEqualTo(SENT_AT);
        assertThat(message.status()).isEqualTo(MessageStatus.READ);
    }

    /**
     * Проверяет перенос доменной модели в сущность.
     */
    @Test
    void applyMapsAllFields() {
        Message message = new Message(3L, 1L, 10L, "hello", SENT_AT, MessageStatus.DELIVERED);
        MessageEntity entity = new MessageEntity();

        mapper.apply(message, entity);

        assertThat(entity.getId()).isEqualTo(3L);
        assertThat(entity.getSenderId()).isEqualTo(1L);
        assertThat(entity.getChatId()).isEqualTo(10L);
        assertThat(entity.getContent()).isEqualTo("hello");
        assertThat(entity.getTimestamp()).isEqualTo(SENT_AT);
        assertThat(entity.getStatus()).isEqualTo("delivered");
    }
}
