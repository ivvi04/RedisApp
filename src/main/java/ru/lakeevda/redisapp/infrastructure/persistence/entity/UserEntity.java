package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import ru.lakeevda.redisapp.domain.enums.UserRole;

import java.time.Instant;

/**
 * JPA-сущность пользователя.
 *
 * @author lakeev-da
 */
@Data
@Entity
@Table(schema = "redis_app", name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private Instant createdAt;

    @Column(nullable = false)
    private String role;

    /**
     * Устанавливает роль пользователя по умолчанию перед сохранением, если она не задана.
     */
    @PrePersist
    protected void onCreate() {
        if (role == null) {
            role = UserRole.USER.getValue();
        }
    }
}
