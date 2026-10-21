package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.dto.chat.ChatResponse;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.ChatType;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.mapper.ChatMapper;
import ru.lakeevda.redisapp.repository.ChatRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CACHE_CHATS = "chats";
    private static final String CACHE_SEARCH = "chatSearch";

    private final ChatRepository chatRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_CHATS, key = "#id", unless = "#result == null")
    public ChatResponse getById(Long id) {
        return ChatMapper.fromEntity(getEntityById(id));
    }

    @Transactional
    @CachePut(cacheNames = CACHE_CHATS, key = "#result.id()")
    public ChatResponse create(ChatCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }

        ChatEntity chat = new ChatEntity();

        chat.setName(request.name());
        chat.setType(ChatType.fromValue(request.type()).getValue());

        Set<UserEntity> members = new HashSet<>();

        for (Long memberId : request.memberIds()) {
            members.add(userService.getEntityById(memberId));
        }

        UserEntity creator = userService.getEntityById(request.createdById());
        members.add(creator);
        chat.setCreatedById(creator.getId());

        chat.setMembers(members);

        return ChatMapper.fromEntity(chatRepository.save(chat));
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_CHATS, key = "#id"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void manageMember(Long id, Long userId, String action) {
        ChatEntity chat = getEntityById(id);
        UserEntity user = userService.getEntityById(userId);

        switch (action) {
            case "ADD" -> chat.getMembers().add(user);
            case "REMOVE" -> chat.getMembers().remove(user);
            default -> throw new IllegalArgumentRequestException();
        }
        chatRepository.save(chat);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_CHATS, key = "#id"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void delete(Long id) {
        ChatEntity chat = getEntityById(id);
        chat.setType(ChatType.DELETED.getValue());
        chatRepository.save(chat);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SEARCH,
            key = "#query + '_' + #chatId",
            condition = "#query != null and #query != ''",
            unless = "#result == null or #result.isEmpty()")
    public List<ChatResponse> searchChats(String query, Long chatId) {
        return chatRepository.searchChats(query, chatId)
                .stream()
                .map(ChatMapper::fromEntity)
                .toList();
    }

    public ChatEntity getEntityById(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));
    }
}