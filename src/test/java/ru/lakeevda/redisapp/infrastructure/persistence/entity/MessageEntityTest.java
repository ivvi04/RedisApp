package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты жизненного цикла JPA-сущности сообщения.
 *
 * @author lakeev-da
 */
class MessageEntityTest {

    /**
     * Проверяет установку времени отправки перед сохранением.
     */
    @Test
    void onCreateSetsTimestamp() {
        MessageEntity entity = new MessageEntity();

        entity.onCreate();

        assertThat(entity.getTimestamp()).isNotNull();
    }

    /**
     * Проверяет сохранение явно заданного времени отправки.
     */
    @Test
    void onCreateKeepsExistingTimestamp() {
        Instant timestamp = Instant.parse("2026-01-01T10:00:00Z");
        MessageEntity entity = new MessageEntity();
        entity.setTimestamp(timestamp);

        entity.onCreate();

        assertThat(entity.getTimestamp()).isEqualTo(timestamp);
    }
}
