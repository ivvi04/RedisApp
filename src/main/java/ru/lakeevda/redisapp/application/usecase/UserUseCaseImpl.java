package ru.lakeevda.redisapp.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.command.UpdateUserCommand;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.application.port.out.PasswordEncoder;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.exception.ResourceAlreadyExistsException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.User;

import java.time.Instant;
import java.util.Locale;

/**
 * Реализация сценариев управления пользователями.
 *
 * @author lakeev-da
 */
@Service
@RequiredArgsConstructor
public class UserUseCaseImpl implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    @Override
    @Transactional(readOnly = true)
    public User getById(Long id) {
        return getUserOrThrow(id);
    }

    /**
     * Возвращает страницу пользователей.
     *
     * @param query параметры выборки
     * @return страница пользователей
     */
    @Override
    @Transactional(readOnly = true)
    public PageResult<User> getAllUsers(PageQuery query) {
        return userRepository.findAll(query);
    }

    /**
     * Создаёт пользователя.
     *
     * @param command команда создания
     * @return созданный пользователь
     * @throws ResourceAlreadyExistsException если пользователь с таким email уже существует
     */
    @Override
    @Transactional
    public User create(CreateUserCommand command) {
        String email = normalizeEmail(command.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User user = new User(
                null,
                email,
                command.username().trim(),
                passwordEncoder.encode(command.password()),
                Instant.now(),
                UserRole.USER);

        return userRepository.save(user);
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param id      идентификатор пользователя
     * @param command команда обновления
     * @return обновлённый пользователь
     * @throws ResourceAlreadyExistsException если email уже занят другим пользователем
     */
    @Override
    @Transactional
    public User update(Long id, UpdateUserCommand command) {
        User existing = getUserOrThrow(id);
        String email = normalizeEmail(command.email());

        if (!existing.email().equals(email) && userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Пользователь с таким email уже существует");
        }

        User updated = new User(
                existing.id(),
                email,
                command.username().trim(),
                existing.password(),
                existing.createdAt(),
                existing.role());

        return userRepository.save(updated);
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     */
    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Возвращает пользователя или выбрасывает исключение.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     * @throws ResourceNotFoundException если пользователь не найден
     */
    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден: " + id));
    }

    /**
     * Приводит email к нижнему регистру и удаляет пробелы по краям.
     *
     * @param email исходный email
     * @return нормализованный email
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
