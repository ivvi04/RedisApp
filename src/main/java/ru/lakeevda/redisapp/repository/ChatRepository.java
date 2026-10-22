package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.lakeevda.redisapp.entity.ChatEntity;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatEntity, Long> {
    
    @Query("SELECT c FROM ChatEntity c WHERE c.name LIKE %:query% OR c.id = :chatId")
    List<ChatEntity> searchChats(String query, Long chatId);

    @Query("""
            SELECT DISTINCT c FROM ChatEntity c
            JOIN c.members m
            WHERE m.id = :userId AND c.type <> :excludedType
            ORDER BY c.createdAt DESC
            """)
    List<ChatEntity> findByMemberIdAndTypeNot(Long userId, String excludedType);

    List<ChatEntity> findByTypeNotOrderByCreatedAtDesc(String excludedType);
}