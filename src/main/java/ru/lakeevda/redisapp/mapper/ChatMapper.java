package ru.lakeevda.redisapp.mapper;

import ru.lakeevda.redisapp.dto.ChatResponse;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.UserEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class ChatMapper {

    public static ChatResponse fromEntity(ChatEntity chat) {

        Set<Long> memberIds = chat.getMembers().stream().map(UserEntity::getId).collect(Collectors.toSet());
        return new ChatResponse(
                chat.getId(),
                chat.getName(),
                chat.getType(),
                memberIds,
                chat.getCreatedAt());
    }
}
