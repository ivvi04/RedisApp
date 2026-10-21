package ru.lakeevda.redisapp.controller;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.lakeevda.redisapp.exception.ChatDeletedException;
import ru.lakeevda.redisapp.exception.MemberNotFoundException;

import java.util.Arrays;
import java.util.stream.Collectors;

@RestControllerAdvice
public class AdviceController {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalArgument(IllegalArgumentException e) {
        return e.getMessage();
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String methodValidation(HandlerMethodValidationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String methodArgumentNotValid(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String constraintViolation(ConstraintViolationException e) {
        return e.getMessage();
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String entityExists(EntityExistsException e) {
        return e.getMessage();
    }

    @ExceptionHandler(ChatDeletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String chatDeleted(ChatDeletedException e) {
        return e.getMessage();
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String memberNotFound(MemberNotFoundException e) {
        return e.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String otherException(RuntimeException e) {
        return "Неизвестная ошибка: " + Arrays.toString(e.getStackTrace());
    }
}
