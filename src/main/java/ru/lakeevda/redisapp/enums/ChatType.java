package ru.lakeevda.redisapp.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ChatType {
    PRIVATE("private"),
    GROUP("group"),
    DELETED("deleted");

    private final String value;

    ChatType(String value) {
        this.value = value;
    }

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