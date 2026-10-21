package ru.lakeevda.redisapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.dto.message.MessageSendRequest;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.service.MessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public MessageResponse send(@RequestBody MessageSendRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }
        return messageService.send(request.chatId(), request.senderId(), request.content());
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageResponse> getByChatId(@PathVariable Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentRequestException();
        }
        return messageService.getByChatId(chatId);
    }

    @GetMapping("/chat/{chatId}/search")
    public List<MessageResponse> searchInChat(@PathVariable Long chatId, @RequestParam String query) {
        if (chatId == null || query == null) {
            throw new IllegalArgumentRequestException();
        }
        return messageService.searchInChat(chatId, query);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long messageId, @RequestParam Long userId) {
        if (messageId == null || userId == null) {
            throw new IllegalArgumentRequestException();
        }
        try {
            messageService.markAsRead(messageId, userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<String> delete(@PathVariable Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentRequestException();
        }
        try {
            messageService.delete(messageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}