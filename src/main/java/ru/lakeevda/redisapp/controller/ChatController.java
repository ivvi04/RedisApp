package ru.lakeevda.redisapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.dto.chat.ChatManageMembersRequest;
import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.service.ChatService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatResponse create(@RequestBody ChatCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }
        return chatService.create(request);
    }

    @GetMapping("/{chatId}")
    public ChatResponse getById(@PathVariable Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentRequestException();
        }
        return chatService.getById(chatId);
    }

    @PutMapping("/members")
    public void manageMembers(@RequestBody ChatManageMembersRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }
        chatService.manageMember(request.chatId(), request.userId(), request.action());
    }

    @DeleteMapping("/{chatId}")
    public void delete(@PathVariable Long chatId) {
        if (chatId == null) {
            throw new IllegalArgumentRequestException();
        }
        chatService.delete(chatId);
    }
}