package ru.lakeevda.redisapp.application.model;

/**
 * Описание сортировки для постраничной выборки.
 *
 * @param property  свойство сортировки
 * @param direction направление сортировки
 * @author lakeev-da
 */
public record SortOrder(String property, Direction direction) {

    /**
     * Направление сортировки.
     */
    public enum Direction {
        ASC,
        DESC
    }
}
