package ru.lakeevda.redisapp.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA-сущность сообщения.
 *
 * @author lakeev-da
 */
@Getter
@Setter
@Entity
@Table(schema = "redis_app", name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private String status;

    /**
     * Устанавливает время отправки сообщения перед сохранением, если оно не задано.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}
