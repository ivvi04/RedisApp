package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.dto.ChatCreateRequest;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.ChatType;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.repository.ChatRepository;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserService userService;


    @Transactional(readOnly = true)
    public ChatEntity getById(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    }

    @Transactional
    public ChatEntity create(ChatCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }

        ChatEntity chat = new ChatEntity();

        chat.setName(request.name());
        chat.setType(ChatType.fromValue(request.type()).getValue());
        chat.setCreatedAt(Instant.now());

        Set<UserEntity> members = new HashSet<>();

        for (Long memberId : request.memberIds()) {
            UserEntity user = userService.getById(memberId);
            if (user != null) members.add(user);
        }

        if (request.createdById() != null) {
            UserEntity creator = userService.getById(request.createdById());
            if (creator != null) {
                members.add(creator);
                chat.setCreatedById(creator.getId());
            }
        }

        chat.setMembers(members);

        return chatRepository.save(chat);
    }

    @Transactional
    public void manageMember(Long chatId, Long userId, String action) {
        ChatEntity chat = getById(chatId);
        UserEntity user = userService.getById(userId);

        switch (action) {
            case "ADD" -> chat.getMembers().add(user);
            case "REMOVE" -> chat.getMembers().remove(user);
        }
        chatRepository.save(chat);
    }

    @Transactional
    public void delete(Long chatId) {
        ChatEntity chat = getById(chatId);
        chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    public List<ChatEntity> searchChats(String query, Long chatId) {
        return chatRepository.searchChats(query, chatId);
    }
}