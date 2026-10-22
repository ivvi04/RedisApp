package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.model.SortOrder;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.UserPersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.UserJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Адаптер хранения пользователей на основе Spring Data JPA.
 *
 * @author lakeev-da
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository repository;
    private final UserPersistenceMapper mapper;

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return найденный пользователь
     */
    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * Находит пользователя по email без учёта регистра.
     *
     * @param email email пользователя
     * @return найденный пользователь
     */
    @Override
    public Optional<User> findByEmailIgnoreCase(String email) {
        return repository.findByEmailIgnoreCase(email).map(mapper::toDomain);
    }

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email пользователя
     * @return {@code true}, если пользователь существует
     */
    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    /**
     * Возвращает страницу пользователей.
     *
     * @param query параметры выборки
     * @return страница пользователей
     */
    @Override
    public PageResult<User> findAll(PageQuery query) {
        Pageable pageable = toPageable(query);
        Page<UserEntity> page = repository.findAll(pageable);
        List<User> content = page.getContent().stream().map(mapper::toDomain).toList();

        return new PageResult<>(
                content,
                page.getTotalElements(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages());
    }

    /**
     * Сохраняет пользователя.
     *
     * @param user пользователь
     * @return сохранённый пользователь
     */
    @Override
    public User save(User user) {
        UserEntity entity = user.id() == null
                ? new UserEntity()
                : repository.findById(user.id()).orElseGet(UserEntity::new);

        mapper.apply(user, entity);
        return mapper.toDomain(repository.save(entity));
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * Преобразует параметры выборки в Spring Data Pageable.
     *
     * @param query параметры выборки
     * @return параметры страницы
     */
    private Pageable toPageable(PageQuery query) {
        Sort sort = query.sort().isEmpty()
                ? Sort.unsorted()
                : Sort.by(query.sort().stream()
                        .map(this::toOrder)
                        .toList());
        return PageRequest.of(query.page(), query.size(), sort);
    }

    /**
     * Преобразует правило сортировки в Spring Data Order.
     *
     * @param order правило сортировки
     * @return заказ сортировки
     */
    private Sort.Order toOrder(SortOrder order) {
        Sort.Direction direction = order.direction() == SortOrder.Direction.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return new Sort.Order(direction, order.property());
    }
}
