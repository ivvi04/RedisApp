package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ChatMapper {

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
