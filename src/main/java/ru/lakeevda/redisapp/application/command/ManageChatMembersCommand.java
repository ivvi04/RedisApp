package ru.lakeevda.redisapp.application.command;

import ru.lakeevda.redisapp.domain.enums.ChatMemberAction;

/**
 * Команда изменения состава участников чата.
 *
 * @param chatId идентификатор чата
 * @param userId идентификатор пользователя
 * @param action действие над участником
 * @author lakeev-da
 */
public record ManageChatMembersCommand(Long chatId, Long userId, ChatMemberAction action) {
}
