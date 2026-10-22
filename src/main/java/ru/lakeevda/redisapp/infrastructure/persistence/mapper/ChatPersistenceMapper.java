package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Преобразование JPA-сущности чата и доменной модели.
 *
 * @author lakeev-da
 */
@Component
public class ChatPersistenceMapper {

    /**
     * Преобразует JPA-сущность в доменную модель.
     *
     * @param entity JPA-сущность
     * @return доменная модель чата
     */
    public Chat toDomain(ChatEntity entity) {
        Set<Long> memberIds = entity.getMembers().stream()
                .map(UserEntity::getId)
                .collect(Collectors.toSet());

        return new Chat(
                entity.getId(),
                entity.getName(),
                ChatType.fromValue(entity.getType()),
                entity.getCreatedAt(),
                entity.getCreatedById(),
                memberIds);
    }

    /**
     * Переносит данные доменной модели в JPA-сущность.
     *
     * @param chat    доменная модель
     * @param entity  JPA-сущность
     * @param members управляемые сущности участников
     */
    public void apply(Chat chat, ChatEntity entity, Set<UserEntity> members) {
        entity.setId(chat.id());
        entity.setName(chat.name());
        entity.setType(chat.type().getValue());
        entity.setCreatedAt(chat.createdAt());
        entity.setCreatedById(chat.createdById());
        entity.getMembers().clear();
        entity.getMembers().addAll(members);
    }
}
