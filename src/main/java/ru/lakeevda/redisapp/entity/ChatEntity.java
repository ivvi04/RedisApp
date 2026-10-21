package ru.lakeevda.redisapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.lakeevda.redisapp.enums.ChatType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Table(schema = "redis_app", name = "chats")
public class ChatEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name")
    private String name;

    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by_id", nullable = false)
    private Long createdById;
    
    @ManyToMany
    @JoinTable(
        name = "chat_users",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @OrderBy("username ASC")
    private Set<UserEntity> members = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}