package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.enums.ChatType;

import java.util.List;
import java.util.Optional;

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
}