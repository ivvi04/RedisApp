package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты жизненного цикла JPA-сущности чата.
 *
 * @author lakeev-da
 */
class ChatEntityTest {

    /**
     * Проверяет установку даты создания перед сохранением.
     */
    @Test
    void onCreateSetsCreatedAt() {
        ChatEntity entity = new ChatEntity();

        entity.onCreate();

        assertThat(entity.getCreatedAt()).isNotNull();
    }

    /**
     * Проверяет сохранение явно заданной даты создания.
     */
    @Test
    void onCreateKeepsExistingCreatedAt() {
        Instant createdAt = Instant.parse("2026-01-01T10:00:00Z");
        ChatEntity entity = new ChatEntity();
        entity.setCreatedAt(createdAt);

        entity.onCreate();

        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    /**
     * Проверяет, что коллекция участников инициализирована.
     */
    @Test
    void membersInitializedByDefault() {
        assertThat(new ChatEntity().getMembers()).isEmpty();
    }
}
