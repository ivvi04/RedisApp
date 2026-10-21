package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.dto.message.MessageResponse;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.MessageEntity;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.ChatType;
import ru.lakeevda.redisapp.enums.MessageStatus;
import ru.lakeevda.redisapp.exception.ChatDeletedException;
import ru.lakeevda.redisapp.exception.MemberNotFoundException;
import ru.lakeevda.redisapp.mapper.MessageMapper;
import ru.lakeevda.redisapp.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "messagesCache")
public class MessageService {

    private static final String CACHE_MESSAGES = "messages";
    private static final String CACHE_SEARCH = "messageSearch";

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final UserService userService;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, key = "#chatId"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public MessageResponse send(Long chatId, Long senderId, String content) {
        ChatEntity chat = chatService.getEntityById(chatId);
        if (chat != null && ChatType.fromValue(chat.getType()) == ChatType.DELETED) {
            throw new ChatDeletedException("Chat has been deleted");
        }

        UserEntity sender = userService.getEntityById(senderId);

        MessageEntity message = new MessageEntity();
        message.setChatId(chatId);
        message.setSenderId(sender.getId());
        message.setContent(content);
        message.setStatus(MessageStatus.SENT.toString());

        return MessageMapper.fromEntity(messageRepository.save(message));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_MESSAGES, key = "#chatId", condition = "#chatId != null")
    public List<MessageResponse> getByChatId(Long chatId) {
        return messageRepository.findByChatIdAndDeletedFalse(chatId)
                .stream()
                .map(MessageMapper::fromEntity)
                .toList();
    }

    @Transactional
    @CacheEvict(cacheNames = CACHE_MESSAGES, key = "#result.chatId()")
    public void markAsRead(Long id, Long userId) {
        MessageEntity message = getById(id);
        ChatEntity chat = chatService.getEntityById(message.getChatId());
        UserEntity user = userService.getEntityById(userId);
        if (!chat.getMembers().contains(user)) {
            throw new MemberNotFoundException("You are not a member of this chat");
        }

        message.setStatus(MessageStatus.READ.toString());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, key = "#result"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void delete(Long id) {
        MessageEntity message = getById(id);
        message.setStatus(MessageStatus.DELETED.toString());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SEARCH,
            key = "#chatId + '_' + #query",
            condition = "#query != null and #query != ''",
            unless = "#result == null or #result.isEmpty()")
    public List<MessageResponse> searchInChat(Long chatId, String query) {
        return messageRepository.searchMessagesInChat(chatId, "%" + query.toLowerCase() + "%")
                .stream().map(MessageMapper::fromEntity)
                .toList();
    }

    private MessageEntity getById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }
}