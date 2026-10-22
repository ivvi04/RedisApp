package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты маппера пользователей.
 *
 * @author lakeev-da
 */
class UserPersistenceMapperTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    private final UserPersistenceMapper mapper = new UserPersistenceMapper();

    /**
     * Проверяет преобразование сущности в доменную модель.
     */
    @Test
    void toDomainMapsAllFields() {
        UserEntity entity = new UserEntity();
        entity.setId(5L);
        entity.setEmail("user@example.com");
        entity.setUsername("Alice");
        entity.setPassword("hash");
        entity.setCreatedAt(CREATED_AT);
        entity.setRole("admin");

        User user = mapper.toDomain(entity);

        assertThat(user.id()).isEqualTo(5L);
        assertThat(user.email()).isEqualTo("user@example.com");
        assertThat(user.username()).isEqualTo("Alice");
        assertThat(user.password()).isEqualTo("hash");
        assertThat(user.createdAt()).isEqualTo(CREATED_AT);
        assertThat(user.role()).isEqualTo(UserRole.ADMIN);
    }

    /**
     * Проверяет роль по умолчанию при отсутствии значения в сущности.
     */
    @Test
    void toDomainUsesUserRoleWhenRoleIsNull() {
        UserEntity entity = new UserEntity();
        entity.setId(5L);

        assertThat(mapper.toDomain(entity).role()).isEqualTo(UserRole.USER);
    }

    /**
     * Проверяет перенос доменной модели в сущность.
     */
    @Test
    void applyMapsAllFields() {
        User user = new User(5L, "user@example.com", "Alice", "hash", CREATED_AT, UserRole.ADMIN);
        UserEntity entity = new UserEntity();

        mapper.apply(user, entity);

        assertThat(entity.getId()).isEqualTo(5L);
        assertThat(entity.getEmail()).isEqualTo("user@example.com");
        assertThat(entity.getUsername()).isEqualTo("Alice");
        assertThat(entity.getPassword()).isEqualTo("hash");
        assertThat(entity.getCreatedAt()).isEqualTo(CREATED_AT);
        assertThat(entity.getRole()).isEqualTo("admin");
    }

    /**
     * Проверяет роль по умолчанию при отсутствии роли в доменной модели.
     */
    @Test
    void applyUsesUserRoleWhenRoleIsNull() {
        User user = new User(5L, "user@example.com", "Alice", "hash", CREATED_AT, null);
        UserEntity entity = new UserEntity();

        mapper.apply(user, entity);

        assertThat(entity.getRole()).isEqualTo(UserRole.USER.getValue());
    }
}
