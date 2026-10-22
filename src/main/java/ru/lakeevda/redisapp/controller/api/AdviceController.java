package ru.lakeevda.redisapp.controller.api;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import ru.lakeevda.redisapp.exception.ChatDeletedException;
import ru.lakeevda.redisapp.exception.MemberNotFoundException;

import java.util.stream.Collectors;

/**
 * Глобальный обработчик исключений REST API.
 *
 * @author lakeev-da
 */
@Slf4j
@RestControllerAdvice
public class AdviceController {

    /**
     * Обрабатывает некорректные аргументы запроса.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalArgument(IllegalArgumentException e) {
        return e.getMessage();
    }

    /**
     * Обрабатывает ошибки валидации параметров метода.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String methodValidation(HandlerMethodValidationException e) {
        log.debug("Ошибка валидации метода: {}", e.getMessage());
        return "Некорректные параметры запроса";
    }

    /**
     * Обрабатывает ошибки валидации тела запроса.
     *
     * @param e исключение
     * @return список ошибок по полям
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String methodArgumentNotValid(MethodArgumentNotValidException e) {
        return e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
    }

    /**
     * Обрабатывает нарушения ограничений валидации.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String constraintViolation(ConstraintViolationException e) {
        log.debug("Ошибка валидации ограничений: {}", e.getMessage());
        return "Некорректные параметры запроса";
    }

    /**
     * Обрабатывает ошибки чтения тела HTTP-запроса.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String messageNotReadable(HttpMessageNotReadableException e) {
        log.debug("Некорректное тело запроса: {}", e.getMessage());
        return "Некорректное тело запроса";
    }

    /**
     * Обрабатывает попытку создать уже существующую сущность.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String entityExists(EntityExistsException e) {
        return e.getMessage();
    }

    /**
     * Обрабатывает обращение к удалённому чату.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(ChatDeletedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String chatDeleted(ChatDeletedException e) {
        return e.getMessage();
    }

    /**
     * Обрабатывает отсутствие сущности.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFound(EntityNotFoundException e) {
        return e.getMessage();
    }

    /**
     * Обрабатывает отсутствие участника чата.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String memberNotFound(MemberNotFoundException e) {
        return e.getMessage();
    }

    /**
     * Обрабатывает все необработанные исключения.
     *
     * @param e исключение
     * @return текст ошибки
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String otherException(RuntimeException e) {
        log.error("Необработанное исключение", e);
        return "Внутренняя ошибка сервера";
    }
}
