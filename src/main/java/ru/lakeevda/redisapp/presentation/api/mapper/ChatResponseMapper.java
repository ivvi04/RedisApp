package ru.lakeevda.redisapp.presentation.api.mapper;

import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatResponse;

/**
 * Преобразование доменной модели чата в DTO.
 *
 * @author lakeev-da
 */
public final class ChatResponseMapper {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private ChatResponseMapper() {
    }

    /**
     * Преобразует доменную модель чата в DTO.
     *
     * @param chat доменная модель
     * @return DTO чата
     */
    public static ChatResponse fromDomain(Chat chat) {
        return new ChatResponse(
                chat.id(),
                chat.name(),
                chat.type().getValue(),
                chat.memberIds(),
                chat.createdAt());
    }
}
