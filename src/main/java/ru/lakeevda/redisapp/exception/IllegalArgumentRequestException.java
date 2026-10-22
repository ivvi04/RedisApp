package ru.lakeevda.redisapp.exception;

public class IllegalArgumentRequestException extends IllegalArgumentException {
    private static final String MESSAGE = "Аргумент запроса не может быть пустым";
    public IllegalArgumentRequestException() {
        super(MESSAGE);
    }
}
