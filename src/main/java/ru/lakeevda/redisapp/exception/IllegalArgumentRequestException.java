package ru.lakeevda.redisapp.exception;

public class IllegalArgumentRequestException extends IllegalArgumentException {
    private static final String MESSAGE = "Illegal request argument cannot be null";
    public IllegalArgumentRequestException() {
        super(MESSAGE);
    }
}
