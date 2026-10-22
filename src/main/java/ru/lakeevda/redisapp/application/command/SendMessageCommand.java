package ru.lakeevda.redisapp.application.command;

/**
 * Команда отправки сообщения.
 *
 * @param chatId   идентификатор чата
 * @param senderId идентификатор отправителя
 * @param content  текст сообщения
 * @author lakeev-da
 */
public record SendMessageCommand(Long chatId, Long senderId, String content) {
}
