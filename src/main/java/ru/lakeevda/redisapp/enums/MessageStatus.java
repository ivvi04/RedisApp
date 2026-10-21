package ru.lakeevda.redisapp.enums;

import lombok.Getter;

@Getter
public enum MessageStatus {
    SENT("sent"),
    DELIVERED("delivered"),
    READ("read"),
    DELETED("deleted");

    private final String value;

    MessageStatus(String value) {
        this.value = value;
    }
}