package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.entity.MessageEntity;
import ru.lakeevda.redisapp.enums.ChatType;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
    
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId AND m.status != 'deleted' ORDER BY m.timestamp ASC")
    List<MessageEntity> findByChatIdAndDeletedFalse(Long chatId);
    
    @Query("SELECT m FROM MessageEntity m WHERE m.chatId = :chatId AND m.content LIKE %:query% AND m.status != 'deleted' ORDER BY m.timestamp ASC")
    List<MessageEntity> searchMessagesInChat(Long chatId, String query);
}