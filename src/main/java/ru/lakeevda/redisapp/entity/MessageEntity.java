package ru.lakeevda.redisapp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * Сущность сообщения.
 *
 * @author lakeev-da
 */
@Data
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
     * Устанавливает время отправки сообщения перед сохранением.
     */
    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }
}