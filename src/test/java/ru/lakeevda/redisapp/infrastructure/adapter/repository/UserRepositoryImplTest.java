package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.model.SortOrder;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.UserPersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.UserJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты адаптера хранения пользователей.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private UserJpaRepository jpaRepository;

    private UserRepositoryImpl repository;

    /**
     * Создаёт тестируемый адаптер с реальным маппером.
     */
    @BeforeEach
    void setUp() {
        repository = new UserRepositoryImpl(jpaRepository, new UserPersistenceMapper());
    }

    /**
     * Проверяет поиск пользователя по идентификатору с маппингом.
     */
    @Test
    void findByIdMapsEntityToDomain() {
        when(jpaRepository.findById(5L)).thenReturn(Optional.of(entity(5L)));

        Optional<User> result = repository.findById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(5L);
        assertThat(result.get().email()).isEqualTo("user@example.com");
        assertThat(result.get().role()).isEqualTo(UserRole.ADMIN);
    }

    /**
     * Проверяет пустой результат поиска по идентификатору.
     */
    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(jpaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThat(repository.findById(5L)).isEmpty();
    }

    /**
     * Проверяет поиск по email без учёта регистра.
     */
    @Test
    void findByEmailIgnoreCaseDelegatesToRepository() {
        when(jpaRepository.findByEmailIgnoreCase("USER@example.com"))
                .thenReturn(Optional.of(entity(5L)));

        Optional<User> result = repository.findByEmailIgnoreCase("USER@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(5L);
        verify(jpaRepository).findByEmailIgnoreCase("USER@example.com");
    }

    /**
     * Проверяет делегирование проверки существования email.
     */
    @Test
    void existsByEmailDelegatesToRepository() {
        when(jpaRepository.existsByEmail("user@example.com")).thenReturn(true);

        boolean result = repository.existsByEmail("user@example.com");

        assertThat(result).isTrue();
        verify(jpaRepository).existsByEmail("user@example.com");
    }

    /**
     * Проверяет постраничную выборку и сортировку по возрастанию.
     */
    @Test
    void findAllMapsPageAndAscendingSort() {
        PageRequest pageable = PageRequest.of(1, 10);
        Page<UserEntity> page = new PageImpl<>(List.of(entity(5L)), pageable, 25);
        when(jpaRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageQuery query = new PageQuery(1, 10,
                List.of(new SortOrder("username", SortOrder.Direction.ASC)));
        PageResult<User> result = repository.findAll(query);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(25);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalPages()).isEqualTo(3);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(1);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort().getOrderFor("username"))
                .isEqualTo(new Sort.Order(Sort.Direction.ASC, "username"));
    }

    /**
     * Проверяет сортировку по убыванию и отсутствие сортировки.
     */
    @Test
    void findAllSupportsDescendingAndUnsortedQueries() {
        when(jpaRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        repository.findAll(new PageQuery(0, 20,
                List.of(new SortOrder("createdAt", SortOrder.Direction.DESC))));
        repository.findAll(new PageQuery(0, 20, List.of()));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(jpaRepository, org.mockito.Mockito.times(2)).findAll(captor.capture());
        assertThat(captor.getAllValues().get(0).getSort().getOrderFor("createdAt"))
                .isEqualTo(new Sort.Order(Sort.Direction.DESC, "createdAt"));
        assertThat(captor.getAllValues().get(1).getSort().isUnsorted()).isTrue();
    }

    /**
     * Проверяет сохранение нового пользователя.
     */
    @Test
    void saveCreatesNewEntity() {
        when(jpaRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = repository.save(user(null));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(jpaRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo("user@example.com");
        assertThat(saved.getUsername()).isEqualTo("Alice");
        assertThat(saved.getPassword()).isEqualTo("hash");
        assertThat(saved.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(saved.getRole()).isEqualTo("admin");
        assertThat(result.id()).isNull();

        verify(jpaRepository, never()).findById(any(Long.class));
    }

    /**
     * Проверяет обновление существующего пользователя.
     */
    @Test
    void saveUpdatesExistingEntity() {
        UserEntity existing = entity(5L);
        when(jpaRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(jpaRepository.save(existing)).thenReturn(existing);

        User result = repository.save(user(5L));

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.email()).isEqualTo("user@example.com");
        verify(jpaRepository).findById(5L);
        verify(jpaRepository).save(existing);
    }

    /**
     * Проверяет делегирование удаления пользователя.
     */
    @Test
    void deleteByIdDelegatesToRepository() {
        repository.deleteById(5L);

        verify(jpaRepository).deleteById(5L);
    }

    /**
     * Создаёт JPA-сущность пользователя.
     *
     * @param id идентификатор пользователя
     * @return сущность пользователя
     */
    private UserEntity entity(Long id) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail("user@example.com");
        entity.setUsername("Alice");
        entity.setPassword("hash");
        entity.setCreatedAt(CREATED_AT);
        entity.setRole("admin");
        return entity;
    }

    /**
     * Создаёт доменную модель пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    private User user(Long id) {
        return new User(id, "user@example.com", "Alice", "hash", CREATED_AT, UserRole.ADMIN);
    }
}
