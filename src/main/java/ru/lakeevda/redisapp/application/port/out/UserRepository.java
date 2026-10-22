package ru.lakeevda.redisapp.application.port.out;

import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.domain.model.User;

import java.util.Optional;

/**
 * Исходящий порт хранения пользователей.
 *
 * @author lakeev-da
 */
public interface UserRepository {

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     */
    Optional<User> findById(Long id);

    /**
     * Находит пользователя по email без учёта регистра.
     *
     * @param email email пользователя
     * @return найденный пользователь
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email пользователя
     * @return {@code true}, если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Возвращает страницу пользователей.
     *
     * @param query параметры выборки
     * @return страница пользователей
     */
    PageResult<User> findAll(PageQuery query);

    /**
     * Сохраняет пользователя.
     *
     * @param user пользователь
     * @return сохранённый пользователь
     */
    User save(User user);

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    void deleteById(Long id);
}
