package ru.lakeevda.redisapp.presentation.api.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Формирование страницы ответа API.
 *
 * @author lakeev-da
 */
public final class PageResponseMapper {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private PageResponseMapper() {
    }

    /**
     * Создаёт страницу Spring Data с сохранением контракта ответа API.
     *
     * @param content       содержимое страницы
     * @param pageable      параметры страницы
     * @param totalElements общее количество элементов
     * @param <T>           тип элемента
     * @return страница ответа
     */
    public static <T> Page<T> toPage(List<T> content, Pageable pageable, long totalElements) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
