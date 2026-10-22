package ru.lakeevda.redisapp.enums;

import lombok.Getter;

/**
 * Статус сообщения.
 *
 * @author lakeev-da
 */
@Getter
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
}