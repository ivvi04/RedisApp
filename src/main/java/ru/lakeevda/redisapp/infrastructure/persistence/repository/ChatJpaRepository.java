package ru.lakeevda.redisapp.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;

import java.util.List;

/**
 * Spring Data репозиторий чатов.
 *
 * @author lakeev-da
 */
public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    @Query("SELECT c FROM ChatEntity c WHERE c.name LIKE %:query% OR c.id = :chatId")
    List<ChatEntity> searchChats(String query, Long chatId);

    /**
     * Возвращает чаты участника, исключая указанный тип.
     *
     * @param userId       идентификатор пользователя
     * @param excludedType исключаемый тип чата
     * @return список чатов
     */
    @Query("""
            SELECT DISTINCT c FROM ChatEntity c
            JOIN c.members m
            WHERE m.id = :userId AND c.type <> :excludedType
            ORDER BY c.createdAt DESC
            """)
    List<ChatEntity> findByMemberIdAndTypeNot(Long userId, String excludedType);

    /**
     * Возвращает все чаты, кроме указанного типа, от новых к старым.
     *
     * @param excludedType исключаемый тип чата
     * @return список чатов
     */
    List<ChatEntity> findByTypeNotOrderByCreatedAtDesc(String excludedType);
}
