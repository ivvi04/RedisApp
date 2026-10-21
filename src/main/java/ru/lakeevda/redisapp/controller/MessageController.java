package ru.lakeevda.redisapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.dto.message.MessageSendRequest;
import ru.lakeevda.redisapp.service.MessageService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public MessageResponse send(@Valid @RequestBody MessageSendRequest request) {
        return messageService.send(request.chatId(), request.senderId(), request.content());
    }

    @GetMapping("/chat/{chatId}")
    public List<MessageResponse> getByChatId(@PathVariable Long chatId) {
        return messageService.getByChatId(chatId);
    }

    @GetMapping("/chat/{chatId}/search")
    public List<MessageResponse> searchInChat(@PathVariable Long chatId, @RequestParam String query) {
        return messageService.searchInChat(chatId, query);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long messageId, @RequestParam Long userId) {
        messageService.markAsRead(messageId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable Long messageId) {
        messageService.delete(messageId);
        return ResponseEntity.ok().build();
    }
}
