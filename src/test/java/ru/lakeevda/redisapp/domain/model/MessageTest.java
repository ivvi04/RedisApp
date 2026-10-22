package ru.lakeevda.redisapp.domain.model;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты доменной логики сообщения.
 *
 * @author lakeev-da
 */
class MessageTest {

    private static final Instant MESSAGE_TIME = Instant.parse("2026-01-01T11:00:00Z");

    /**
     * Проверяет создание нового отправленного сообщения.
     */
    @Test
    void sentCreatesMessageWithSentStatus() {
        Message message = Message.sent(10L, 2L, "Hello");

        assertThat(message.id()).isNull();
        assertThat(message.chatId()).isEqualTo(10L);
        assertThat(message.senderId()).isEqualTo(2L);
        assertThat(message.content()).isEqualTo("Hello");
        assertThat(message.status()).isEqualTo(MessageStatus.SENT);
        assertThat(message.timestamp()).isNotNull();
    }

    /**
     * Проверяет перевод сообщения в статус прочитанного.
     */
    @Test
    void markReadChangesStatusAndPreservesData() {
        Message message = message(MessageStatus.SENT);

        Message read = message.markRead();

        assertThat(read.status()).isEqualTo(MessageStatus.READ);
        assertThat(read.id()).isEqualTo(message.id());
        assertThat(read.senderId()).isEqualTo(message.senderId());
        assertThat(read.chatId()).isEqualTo(message.chatId());
        assertThat(read.content()).isEqualTo(message.content());
        assertThat(read.timestamp()).isEqualTo(message.timestamp());
    }

    /**
     * Проверяет перевод сообщения в статус удалённого.
     */
    @Test
    void markDeletedChangesStatusAndPreservesData() {
        Message message = message(MessageStatus.READ);

        Message deleted = message.markDeleted();

        assertThat(deleted.status()).isEqualTo(MessageStatus.DELETED);
        assertThat(deleted.id()).isEqualTo(message.id());
        assertThat(deleted.timestamp()).isEqualTo(message.timestamp());
    }

    /**
     * Создаёт тестовую доменную модель сообщения.
     *
     * @param status статус сообщения
     * @return сообщение
     */
    private Message message(MessageStatus status) {
        return new Message(99L, 2L, 10L, "Hello", MESSAGE_TIME, status);
    }
}
