package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.entity.MessageEntity;
import ru.lakeevda.redisapp.enums.ChatType;

import java.util.List;

/**
 * Репозиторий для работы с сообщениями.
 *
 * @author lakeev-da
 */
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    /**
     * Возвращает неудалённые сообщения чата в хронологическом порядке.
     *
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId AND m.status != 'deleted' ORDER BY m.timestamp ASC")
    List<MessageEntity> findByChatIdAndDeletedFalse(Long chatId);

    /**
     * Ищет неудалённые сообщения чата по тексту.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId AND m.content LIKE %:query% AND m.status != 'deleted' ORDER BY m.timestamp ASC")
    List<MessageEntity> searchMessagesInChat(Long chatId, String query);
}