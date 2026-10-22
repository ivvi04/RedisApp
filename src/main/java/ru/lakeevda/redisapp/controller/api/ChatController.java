package ru.lakeevda.redisapp.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.dto.chat.ChatManageMembersRequest;
import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatResponse> getMyChats(Authentication authentication) {
        return chatService.getUserChats(Long.valueOf(authentication.getName()));
    }

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
