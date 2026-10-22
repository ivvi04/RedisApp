package ru.lakeevda.redisapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.entity.UserEntity;

import java.util.Optional;

/**
 * Репозиторий для работы с пользователями.
 *
 * @author lakeev-da
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

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
