package ru.lakeevda.redisapp.application.command;

import java.util.Set;

/**
 * Команда создания чата.
 *
 * @param name        название чата
 * @param memberIds   идентификаторы участников
 * @param createdById идентификатор создателя
 * @author lakeev-da
 */
public record CreateChatCommand(String name, Set<Long> memberIds, Long createdById) {
}
