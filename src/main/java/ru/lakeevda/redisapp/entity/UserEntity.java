package ru.lakeevda.redisapp.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDateTime;
import lombok.Data;

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
}
