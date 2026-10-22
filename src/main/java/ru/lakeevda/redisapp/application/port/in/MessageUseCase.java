package ru.lakeevda.redisapp.application.port.in;

import ru.lakeevda.redisapp.application.command.SendMessageCommand;
import ru.lakeevda.redisapp.domain.model.Message;

import java.util.List;

/**
 * Входящий порт сценариев работы с сообщениями.
 *
 * @author lakeev-da
 */
public interface MessageUseCase {

    /**
     * Отправляет сообщение в чат.
     *
     * @param command команда отправки
     * @return сохранённое сообщение
     */
    Message send(SendMessageCommand command);

    /**
     * Возвращает сообщения чата и отмечает прочитанными сообщения,
     * отправленные другими пользователями.
     *
     * @param chatId   идентификатор чата
     * @param readerId идентификатор читателя
     * @return список сообщений
     */
    List<Message> getByChatId(Long chatId, Long readerId);

    /**
     * Отмечает сообщение как прочитанное.
     *
     * @param id     идентификатор сообщения
     * @param userId идентификатор пользователя
     */
    void markAsRead(Long id, Long userId);

    /**
     * Помечает сообщение как удалённое.
     *
     * @param id      идентификатор сообщения
     * @param userId  идентификатор пользователя, удаляющего сообщение
     * @param isAdmin признак администратора
     */
    void delete(Long id, Long userId, boolean isAdmin);

    /**
     * Ищет сообщения внутри чата.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    List<Message> searchInChat(Long chatId, String query);
}
