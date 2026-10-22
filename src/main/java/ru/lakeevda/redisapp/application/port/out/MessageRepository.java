package ru.lakeevda.redisapp.application.port.out;

import ru.lakeevda.redisapp.domain.model.Message;

import java.util.List;
import java.util.Optional;

/**
 * Исходящий порт хранения сообщений.
 *
 * @author lakeev-da
 */
public interface MessageRepository {

    /**
     * Находит сообщение по идентификатору.
     *
     * @param id идентификатор сообщения
     * @return найденное сообщение
     */
    Optional<Message> findById(Long id);

    /**
     * Возвращает неудалённые сообщения чата.
     *
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    List<Message> findByChatId(Long chatId);

    /**
     * Ищет неудалённые сообщения чата по тексту.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    List<Message> searchInChat(Long chatId, String query);

    /**
     * Сохраняет сообщение.
     *
     * @param message сообщение
     * @return сохранённое сообщение
     */
    Message save(Message message);
}
