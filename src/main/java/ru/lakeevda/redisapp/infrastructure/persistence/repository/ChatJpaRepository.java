package ru.lakeevda.redisapp.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data репозиторий чатов.
 *
 * @author lakeev-da
 */
public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {

    /**
     * Находит чат по идентификатору вместе с участниками.
     *
     * @param id идентификатор чата
     * @return найденный чат
     */
    @Override
    @EntityGraph(attributePaths = "members")
    Optional<ChatEntity> findById(Long id);

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    @EntityGraph(attributePaths = "members")
    @Query("SELECT c FROM ChatEntity c WHERE c.name LIKE %:query% OR c.id = :chatId")
    List<ChatEntity> searchChats(String query, Long chatId);

    /**
     * Возвращает чаты участника, исключая указанный тип.
     *
     * @param userId       идентификатор пользователя
     * @param excludedType исключаемый тип чата
     * @return список чатов
     */
    @EntityGraph(attributePaths = "members")
    @Query("""
            SELECT c FROM ChatEntity c
            WHERE c.type <> :excludedType
              AND c.id IN (SELECT c2.id FROM ChatEntity c2 JOIN c2.members m WHERE m.id = :userId)
            ORDER BY c.createdAt DESC
            """)
    List<ChatEntity> findByMemberIdAndTypeNot(Long userId, String excludedType);

    /**
     * Проверяет, что пользователь состоит в неудалённом чате.
     *
     * @param chatId идентификатор чата
     * @param userId идентификатор пользователя
     * @return {@code true}, если пользователь является участником активного чата
     */
    @Query("""
            SELECT COUNT(m) > 0 FROM ChatEntity c JOIN c.members m
            WHERE c.id = :chatId AND m.id = :userId AND c.type <> 'deleted'
            """)
    boolean existsActiveMember(Long chatId, Long userId);

    /**
     * Возвращает все чаты, кроме указанного типа, от новых к старым.
     *
     * @param excludedType исключаемый тип чата
     * @return список чатов
     */
    @EntityGraph(attributePaths = "members")
    List<ChatEntity> findByTypeNotOrderByCreatedAtDesc(String excludedType);
}
