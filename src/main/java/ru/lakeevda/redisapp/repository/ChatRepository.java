package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.enums.ChatType;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    
    @Query("SELECT c FROM ChatEntity c WHERE :userId MEMBER OF c.members AND c.type = :type ORDER BY c.createdAt ASC")
    Optional<ChatEntity> findOrCreatePrivateChat(Long userId, Long otherUserId, ChatType type);
    
    @Query("SELECT c FROM ChatEntity c WHERE c.name LIKE %:query% OR c.id = :chatId")
    List<ChatEntity> searchChats(String query, Long chatId);
}