package ru.lakeevda.redisapp.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Помечает непрочитанные сообщения чата как прочитанные,
     * если их отправителем не является указанный пользователь.
     *
     * @param chatId   идентификатор чата
     * @param readerId идентификатор читателя
     * @return количество обновлённых сообщений
     */
    @Modifying
    @Query("UPDATE MessageEntity m SET m.status = 'read' "
            + "WHERE m.chatId = :chatId "
            + "AND m.senderId <> :readerId "
            + "AND m.status <> 'read' "
            + "AND m.status <> 'deleted'")
    int markReadByChatIdAndSenderNot(@Param("chatId") Long chatId, @Param("readerId") Long readerId);
}
