package ru.lakeevda.redisapp.application.port.in;

import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.command.UpdateUserCommand;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.domain.model.User;

/**
 * Входящий порт сценариев работы с пользователями.
 *
 * @author lakeev-da
 */
public interface UserUseCase {

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    User getById(Long id);

    /**
     * Возвращает страницу пользователей.
     *
     * @param query параметры выборки
     * @return страница пользователей
     */
    PageResult<User> getAllUsers(PageQuery query);

    /**
     * Создаёт пользователя.
     *
     * @param command команда создания
     * @return созданный пользователь
     */
    User create(CreateUserCommand command);

    /**
     * Обновляет пользователя.
     *
     * @param id      идентификатор пользователя
     * @param command команда обновления
     * @return обновлённый пользователь
     */
    User update(Long id, UpdateUserCommand command);

    /**
     * Удаляет пользователя.
     *
     * @param id идентификатор пользователя
     */
    void delete(Long id);
}
