package ru.lakeevda.redisapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.dto.chat.ChatManageMembersRequest;
import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.service.ChatService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse create(@Valid @RequestBody ChatCreateRequest request) {
        return chatService.create(request);
    }

    @GetMapping("/{chatId}")
    public ChatResponse getById(@PathVariable Long chatId) {
        return chatService.getById(chatId);
    }

    @PutMapping("/members")
    public void manageMembers(@Valid @RequestBody ChatManageMembersRequest request) {
        chatService.manageMember(request.chatId(), request.userId(), request.action());
    }

    @DeleteMapping("/{chatId}")
    public void delete(@PathVariable Long chatId) {
        chatService.delete(chatId);
    }
}
