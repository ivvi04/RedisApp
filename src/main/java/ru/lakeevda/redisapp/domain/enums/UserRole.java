package ru.lakeevda.redisapp.domain.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Роль пользователя.
 *
 * @author lakeev-da
 */
public enum UserRole {
    ADMIN("admin"),
    USER("user");

    private final String value;

    /**
     * Создаёт роль с указанным строковым значением.
     *
     * @param value строковое значение роли
     */
    UserRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Преобразует строковое значение в роль пользователя без учёта регистра.
     *
     * @param value строковое значение роли
     * @return роль пользователя
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
    public static UserRole fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Роль не может быть пустой");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(UserRole.values())
                .filter(role -> role.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестная роль: " + value));
    }
}
