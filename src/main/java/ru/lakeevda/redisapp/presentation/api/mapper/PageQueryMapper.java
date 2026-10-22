package ru.lakeevda.redisapp.presentation.api.mapper;

import org.springframework.data.domain.Pageable;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.SortOrder;

import java.util.List;

/**
 * Преобразование параметров страницы Spring Data в прикладную модель.
 *
 * @author lakeev-da
 */
public final class PageQueryMapper {

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private PageQueryMapper() {
    }

    /**
     * Преобразует Pageable в параметры прикладной выборки.
     *
     * @param pageable параметры страницы Spring Data
     * @return параметры выборки
     */
    public static PageQuery fromPageable(Pageable pageable) {
        List<SortOrder> sort = pageable.getSort().stream()
                .map(order -> new SortOrder(
                        order.getProperty(),
                        order.isAscending() ? SortOrder.Direction.ASC : SortOrder.Direction.DESC))
                .toList();

        return new PageQuery(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
