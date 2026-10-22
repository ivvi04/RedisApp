package ru.lakeevda.redisapp.application.model;

import java.util.List;

/**
 * Результат постраничной выборки.
 *
 * @param content       элементы текущей страницы
 * @param totalElements общее количество элементов
 * @param page          номер страницы
 * @param size          размер страницы
 * @param totalPages    общее количество страниц
 * @param <T>           тип элемента
 * @author lakeev-da
 */
public record PageResult<T>(
        List<T> content,
        long totalElements,
        int page,
        int size,
        int totalPages) {
}
