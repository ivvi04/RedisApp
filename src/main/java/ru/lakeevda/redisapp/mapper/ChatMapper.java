package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Преобразование сущности чата в DTO.
 *
 * @author lakeev-da
 */
public class ChatMapper {

    /**
     * Преобразует сущность чата в DTO.
     *
     * @param entity сущность чата
     * @return DTO чата
     */
    public static ChatResponse fromEntity(ChatEntity entity) {

        Set<Long> memberIds = entity.getMembers().stream().map(UserEntity::getId).collect(Collectors.toSet());
        return new ChatResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                memberIds,
                entity.getCreatedAt());
    }
}
