package ru.lakeevda.redisapp.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Тип чата.
 *
 * @author lakeev-da
 */
@Getter
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

    /**
     * Преобразует строковое значение в тип чата.
     *
     * @param value строковое значение типа
     * @return тип чата
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
    public static ChatType fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Тип чата не может быть пустым");
        }
        String normalized = value.trim().toLowerCase();
        return Arrays.stream(ChatType.values())
                .filter(x -> x.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный тип чата: " + value));
    }
}