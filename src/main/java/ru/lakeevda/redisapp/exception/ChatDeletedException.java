package ru.lakeevda.redisapp.exception;

public class ChatDeletedException extends RuntimeException {
    public ChatDeletedException(String message) {
        super(message);
    }
}
