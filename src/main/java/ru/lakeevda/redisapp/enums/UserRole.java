package ru.lakeevda.redisapp.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Роль пользователя.
 *
 * @author lakeev-da
 */
@Getter
public enum UserRole {
    ADMIN("admin"),
    USER("user"),;

    private final String value;

    /**
     * Создаёт роль с указанным строковым значением.
     *
     * @param value строковое значение роли
     */
    UserRole(String value) {
        this.value = value;
    }

    /**
     * Преобразует строковое значение в роль пользователя.
     *
     * @param value строковое значение роли
     * @return роль пользователя
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
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
