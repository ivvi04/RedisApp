package ru.lakeevda.redisapp.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.MessageEntity;

import java.util.List;

/**
 * Spring Data репозиторий сообщений.
 *
 * @author lakeev-da
 */
public interface MessageJpaRepository extends JpaRepository<MessageEntity, Long> {

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
