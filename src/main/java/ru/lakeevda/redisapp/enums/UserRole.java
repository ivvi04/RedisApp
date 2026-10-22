package ru.lakeevda.redisapp.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum UserRole {
    ADMIN("admin"),
    USER("user"),;

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Роль не может быть пустой");
        }
        return Arrays.stream(UserRole.values())
                .filter(x -> x.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная роль: " + value));
    }
}
