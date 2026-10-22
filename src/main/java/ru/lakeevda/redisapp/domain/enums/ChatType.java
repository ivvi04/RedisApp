package ru.lakeevda.redisapp.domain.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Тип чата.
 *
 * @author lakeev-da
 */
public enum ChatType {
    PRIVATE("private"),
    GROUP("group"),
    DELETED("deleted");

    private final String value;

    /**
     * Создаёт тип чата с указанным строковым значением.
     *
     * @param value строковое значение типа
     */
    ChatType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Преобразует строковое значение в тип чата без учёта регистра.
     *
     * @param value строковое значение типа
     * @return тип чата
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
    public static ChatType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Тип чата не может быть пустым");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(ChatType.values())
                .filter(type -> type.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный тип чата: " + value));
    }
}
