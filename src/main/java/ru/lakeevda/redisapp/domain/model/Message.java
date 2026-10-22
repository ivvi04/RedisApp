package ru.lakeevda.redisapp.domain.model;

import ru.lakeevda.redisapp.domain.enums.MessageStatus;

import java.time.Instant;

/**
 * Доменная модель сообщения.
 *
 * @param id        идентификатор сообщения
 * @param senderId  идентификатор отправителя
 * @param chatId    идентификатор чата
 * @param content   текст сообщения
 * @param timestamp время отправки
 * @param status    статус сообщения
 * @author lakeev-da
 */
public record Message(
        Long id,
        Long senderId,
        Long chatId,
        String content,
        Instant timestamp,
        MessageStatus status) {

    /**
     * Создаёт новое отправленное сообщение.
     *
     * @param chatId   идентификатор чата
     * @param senderId идентификатор отправителя
     * @param content  текст сообщения
     * @return новое сообщение
     */
    public static Message sent(Long chatId, Long senderId, String content) {
        return new Message(null, senderId, chatId, content, Instant.now(), MessageStatus.SENT);
    }

    /**
     * Помечает сообщение как прочитанное.
     *
     * @return сообщение со статусом {@code READ}
     */
    public Message markRead() {
        return new Message(id, senderId, chatId, content, timestamp, MessageStatus.READ);
    }

    /**
     * Помечает сообщение как удалённое.
     *
     * @return сообщение со статусом {@code DELETED}
     */
    public Message markDeleted() {
        return new Message(id, senderId, chatId, content, timestamp, MessageStatus.DELETED);
    }
}
