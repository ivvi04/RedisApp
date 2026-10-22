package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import org.junit.jupiter.api.Test;
import ru.lakeevda.redisapp.domain.enums.UserRole;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты жизненного цикла JPA-сущности пользователя.
 *
 * @author lakeev-da
 */
class UserEntityTest {

    /**
     * Проверяет установку роли по умолчанию перед сохранением.
     */
    @Test
    void onCreateSetsDefaultRole() {
        UserEntity entity = new UserEntity();

        entity.onCreate();

        assertThat(entity.getRole()).isEqualTo(UserRole.USER.getValue());
    }

    /**
     * Проверяет сохранение явно заданной роли.
     */
    @Test
    void onCreateKeepsExistingRole() {
        UserEntity entity = new UserEntity();
        entity.setRole(UserRole.ADMIN.getValue());

        entity.onCreate();

        assertThat(entity.getRole()).isEqualTo(UserRole.ADMIN.getValue());
    }
}
