package ru.lakeevda.redisapp.domain.enums;

import java.util.Arrays;
import java.util.Locale;

/**
 * Статус сообщения.
 *
 * @author lakeev-da
 */
public enum MessageStatus {
    SENT("sent"),
    DELIVERED("delivered"),
    READ("read"),
    DELETED("deleted");

    private final String value;

    /**
     * Создаёт статус сообщения с указанным строковым значением.
     *
     * @param value строковое значение статуса
     */
    MessageStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * Преобразует строковое значение в статус сообщения без учёта регистра.
     *
     * @param value строковое значение статуса
     * @return статус сообщения
     * @throws IllegalArgumentException если значение пустое или неизвестно
     */
    public static MessageStatus fromValue(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Статус сообщения не может быть пустым");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(MessageStatus.values())
                .filter(status -> status.value.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный статус сообщения: " + value));
    }
}
