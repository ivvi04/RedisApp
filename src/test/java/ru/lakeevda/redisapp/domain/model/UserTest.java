package ru.lakeevda.redisapp.domain.model;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.UserRole;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты доменной логики пользователя.
 *
 * @author lakeev-da
 */
class UserTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    /**
     * Проверяет замену email с сохранением остальных полей.
     */
    @Test
    void withEmailPreservesOtherFields() {
        User user = user();

        User updated = user.withEmail("new@example.com");

        assertThat(updated.email()).isEqualTo("new@example.com");
        assertThat(updated.id()).isEqualTo(user.id());
        assertThat(updated.username()).isEqualTo(user.username());
        assertThat(updated.password()).isEqualTo(user.password());
        assertThat(updated.createdAt()).isEqualTo(user.createdAt());
        assertThat(updated.role()).isEqualTo(user.role());
    }

    /**
     * Проверяет замену имени с сохранением остальных полей.
     */
    @Test
    void withUsernamePreservesOtherFields() {
        User user = user();

        User updated = user.withUsername("Bob");

        assertThat(updated.username()).isEqualTo("Bob");
        assertThat(updated.email()).isEqualTo(user.email());
        assertThat(updated.id()).isEqualTo(user.id());
        assertThat(updated.role()).isEqualTo(user.role());
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @return пользователь
     */
    private User user() {
        return new User(5L, "user@example.com", "Alice", "hash", CREATED_AT, UserRole.USER);
    }
}
