package ru.lakeevda.redisapp.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

/**
 * Spring Data репозиторий пользователей.
 *
 * @author lakeev-da
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email email пользователя
     * @return {@code true}, если пользователь существует
     */
    boolean existsByEmail(String email);

    /**
     * Ищет пользователя по email без учёта регистра.
     *
     * @param email email пользователя
     * @return найденный пользователь
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
