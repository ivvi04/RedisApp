package ru.lakeevda.redisapp.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;

/**
 * Преобразование JPA-сущности пользователя и доменной модели.
 *
 * @author lakeev-da
 */
@Component
public class UserPersistenceMapper {

    /**
     * Преобразует JPA-сущность в доменную модель.
     *
     * @param entity JPA-сущность
     * @return доменная модель пользователя
     */
    public User toDomain(UserEntity entity) {
        UserRole role = entity.getRole() == null
                ? UserRole.USER
                : UserRole.fromValue(entity.getRole());

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getUsername(),
                entity.getPassword(),
                entity.getCreatedAt(),
                role);
    }

    /**
     * Переносит данные доменной модели в JPA-сущность.
     *
     * @param user   доменная модель
     * @param entity JPA-сущность
     */
    public void apply(User user, UserEntity entity) {
        entity.setId(user.id());
        entity.setEmail(user.email());
        entity.setUsername(user.username());
        entity.setPassword(user.password());
        entity.setCreatedAt(user.createdAt());
        entity.setRole(user.role() == null ? UserRole.USER.getValue() : user.role().getValue());
    }
}
