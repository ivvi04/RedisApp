package ru.lakeevda.redisapp.entity;

import jakarta.persistence.*;

import java.time.Instant;
import lombok.Data;
import ru.lakeevda.redisapp.enums.UserRole;

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

    @PrePersist
    protected void onCreate() {
        role = UserRole.USER.getValue();
    }
}
