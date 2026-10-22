package ru.lakeevda.redisapp.application.model;

import java.util.List;

/**
 * Параметры постраничной выборки.
 *
 * @param page номер страницы
 * @param size размер страницы
 * @param sort правила сортировки
 * @author lakeev-da
 */
public record PageQuery(int page, int size, List<SortOrder> sort) {

    /**
     * Нормализует параметры выборки.
     */
    public PageQuery {
        sort = sort == null ? List.of() : List.copyOf(sort);
    }
}
