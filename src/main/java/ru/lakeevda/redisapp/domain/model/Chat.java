package ru.lakeevda.redisapp.domain.model;

import ru.lakeevda.redisapp.domain.enums.ChatType;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Доменная модель чата.
 *
 * @param id          идентификатор чата
 * @param name        название чата
 * @param type        тип чата
 * @param createdAt   дата создания
 * @param createdById идентификатор создателя
 * @param memberIds   идентификаторы участников
 * @author lakeev-da
 */
public record Chat(
        Long id,
        String name,
        ChatType type,
        Instant createdAt,
        Long createdById,
        Set<Long> memberIds) {

    /**
     * Нормализует набор участников при создании модели.
     */
    public Chat {
        memberIds = memberIds == null ? new LinkedHashSet<>() : new LinkedHashSet<>(memberIds);
    }

    /**
     * Создаёт новый чат и автоматически добавляет создателя в участники.
     *
     * @param name        название чата
     * @param createdById идентификатор создателя
     * @param memberIds   идентификаторы участников
     * @return новый чат
     */
    public static Chat create(String name, Long createdById, Set<Long> memberIds) {
        Set<Long> ids = memberIds == null ? new HashSet<>() : new HashSet<>(memberIds);
        ids.add(createdById);
        return new Chat(null, name, resolveType(ids), Instant.now(), createdById, ids);
    }

    /**
     * Проверяет, что чат помечен как удалённый.
     *
     * @return {@code true}, если чат удалён
     */
    public boolean isDeleted() {
        return type == ChatType.DELETED;
    }

    /**
     * Проверяет, является ли пользователь участником чата.
     *
     * @param userId идентификатор пользователя
     * @return {@code true}, если пользователь состоит в чате
     */
    public boolean hasMember(Long userId) {
        return memberIds.contains(userId);
    }

    /**
     * Возвращает копию чата с добавленным участником.
     *
     * @param userId идентификатор пользователя
     * @return обновлённый чат
     */
    public Chat addMember(Long userId) {
        Set<Long> ids = new HashSet<>(memberIds);
        ids.add(userId);
        return withMembers(ids);
    }

    /**
     * Возвращает копию чата с удалённым участником.
     *
     * @param userId идентификатор пользователя
     * @return обновлённый чат
     */
    public Chat removeMember(Long userId) {
        Set<Long> ids = new HashSet<>(memberIds);
        ids.remove(userId);
        return withMembers(ids);
    }

    /**
     * Повышает тип чата до группового, если участников стало больше двух.
     * Понижение до приватного не выполняется.
     *
     * @return чат с актуальным типом
     */
    public Chat withActualType() {
        if (!isDeleted() && resolveType(memberIds) == ChatType.GROUP) {
            return new Chat(id, name, ChatType.GROUP, createdAt, createdById, memberIds);
        }
        return this;
    }

    /**
     * Помечает чат как удалённый.
     *
     * @return удалённый чат
     */
    public Chat markDeleted() {
        return new Chat(id, name, ChatType.DELETED, createdAt, createdById, memberIds);
    }

    /**
     * Определяет тип чата по количеству участников.
     *
     * @param memberIds идентификаторы участников
     * @return {@code GROUP}, если участников больше двух, иначе {@code PRIVATE}
     */
    public static ChatType resolveType(Set<Long> memberIds) {
        return memberIds.size() > 2 ? ChatType.GROUP : ChatType.PRIVATE;
    }

    /**
     * Возвращает копию чата с новым набором участников.
     *
     * @param ids идентификаторы участников
     * @return обновлённый чат
     */
    private Chat withMembers(Set<Long> ids) {
        return new Chat(id, name, type, createdAt, createdById, ids);
    }
}
