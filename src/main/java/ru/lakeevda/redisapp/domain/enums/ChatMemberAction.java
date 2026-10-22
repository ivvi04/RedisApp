package ru.lakeevda.redisapp.domain.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Действие над участником чата.
 *
 * @author lakeev-da
 */
public enum ChatMemberAction {
    ADD,
    REMOVE;

    /**
     * Преобразует строковое значение в действие над участником.
     *
     * @param value строковое значение действия
     * @return действие над участником
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
    public static ChatMemberAction fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Действие не может быть пустым");
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(ChatMemberAction.values())
                .filter(action -> action.name().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное действие: " + value));
    }
}
