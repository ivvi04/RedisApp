package ru.lakeevda.redisapp.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.command.UpdateUserCommand;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.model.SortOrder;
import ru.lakeevda.redisapp.application.port.out.PasswordEncoder;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.exception.ResourceAlreadyExistsException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Тесты бизнес-логики управления пользователями.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class UserUseCaseImplTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserUseCaseImpl userUseCase;

    /**
     * Проверяет получение пользователя по идентификатору.
     */
    @Test
    void getByIdReturnsExistingUser() {
        User user = user(5L, "user@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        User result = userUseCase.getById(5L);

        assertThat(result).isEqualTo(user);
    }

    /**
     * Проверяет ошибку при получении несуществующего пользователя.
     */
    @Test
    void getByIdThrowsWhenUserNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userUseCase.getById(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 5");
    }

    /**
     * Проверяет делегирование постраничной выборки репозиторию.
     */
    @Test
    void getAllUsersDelegatesToRepository() {
        PageQuery query = new PageQuery(1, 10, List.of(new SortOrder("email", SortOrder.Direction.ASC)));
        PageResult<User> page = new PageResult<>(List.of(user(1L, "user@example.com")), 1, 1, 10, 1);
        when(userRepository.findAll(query)).thenReturn(page);

        PageResult<User> result = userUseCase.getAllUsers(query);

        assertThat(result).isEqualTo(page);
        verify(userRepository).findAll(query);
    }

    /**
     * Проверяет нормализацию email, хеширование пароля и параметры создаваемого пользователя.
     */
    @Test
    void createNormalizesEmailAndEncodesPassword() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            return new User(10L, saved.email(), saved.username(), saved.password(),
                    saved.createdAt(), saved.role());
        });

        User result = userUseCase.create(new CreateUserCommand("  USER@Example.COM ", " Alice ", "secret"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.id()).isNull();
        assertThat(saved.email()).isEqualTo("user@example.com");
        assertThat(saved.username()).isEqualTo("Alice");
        assertThat(saved.password()).isEqualTo("hashed-password");
        assertThat(saved.role()).isEqualTo(UserRole.USER);
        assertThat(saved.createdAt()).isNotNull();

        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.email()).isEqualTo("user@example.com");
    }

    /**
     * Проверяет запрет создания пользователя с занятым email.
     */
    @Test
    void createThrowsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userUseCase.create(
                new CreateUserCommand("USER@example.com", "Alice", "secret")))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("email уже существует");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Проверяет обновление пользователя с сохранением неизменяемых полей.
     */
    @Test
    void updateNormalizesEmailAndPreservesImmutableFields() {
        User existing = new User(5L, "old@example.com", "Old", "hash", CREATED_AT, UserRole.ADMIN);
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userUseCase.update(5L, new UpdateUserCommand(" New@Example.com ", " Bob "));

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.username()).isEqualTo("Bob");
        assertThat(result.password()).isEqualTo("hash");
        assertThat(result.createdAt()).isEqualTo(CREATED_AT);
        assertThat(result.role()).isEqualTo(UserRole.ADMIN);
    }

    /**
     * Проверяет, что при неизменном email репозиторий не проверяет его занятость.
     */
    @Test
    void updateKeepsSameEmailWithoutExistsCheck() {
        User existing = user(5L, "user@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userUseCase.update(5L, new UpdateUserCommand("USER@example.com", "Alice"));

        assertThat(result.email()).isEqualTo("user@example.com");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    /**
     * Проверяет запрет смены email на уже занятый.
     */
    @Test
    void updateThrowsWhenNewEmailAlreadyExists() {
        User existing = user(5L, "old@example.com");
        when(userRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userUseCase.update(
                5L, new UpdateUserCommand("taken@example.com", "Alice")))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("email уже существует");

        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Проверяет ошибку обновления несуществующего пользователя.
     */
    @Test
    void updateThrowsWhenUserNotFound() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userUseCase.update(
                5L, new UpdateUserCommand("new@example.com", "Alice")))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Проверяет делегирование удаления пользователя репозиторию.
     */
    @Test
    void deleteDelegatesToRepository() {
        userUseCase.delete(5L);

        verify(userRepository).deleteById(5L);
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @param id    идентификатор пользователя
     * @param email email пользователя
     * @return пользователь
     */
    private User user(Long id, String email) {
        return new User(id, email, "Alice", "hash", CREATED_AT, UserRole.USER);
    }
}
