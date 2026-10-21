package ru.lakeevda.redisapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.lakeevda.redisapp.enums.MessageStatus;

import java.time.LocalDateTime;

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
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}