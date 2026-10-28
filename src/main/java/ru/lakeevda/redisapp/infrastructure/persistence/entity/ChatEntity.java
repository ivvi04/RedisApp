package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA-сущность чата.
 *
 * @author lakeev-da
 */
@Getter
@Setter
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
    @JoinTable(schema = "redis_app", name = "chat_users",
            joinColumns = @JoinColumn(name = "chat_id"), inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @OrderBy("username ASC")
    private Set<UserEntity> members = new HashSet<>();

    /**
     * Устанавливает дату создания чата перед сохранением, если она не задана.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
