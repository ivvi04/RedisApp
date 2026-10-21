package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.entity.ChatEntity;
import ru.lakeevda.redisapp.entity.MessageEntity;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.ChatType;
import ru.lakeevda.redisapp.enums.MessageStatus;
import ru.lakeevda.redisapp.exception.ChatDeletedException;
import ru.lakeevda.redisapp.exception.MemberNotFoundException;
import ru.lakeevda.redisapp.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "messagesCache")
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final UserService userService;

    @Transactional
    public MessageEntity send(Long chatId, Long senderId, String content) {
        ChatEntity chat = chatService.getById(chatId);
        if (chat != null && ChatType.fromValue(chat.getType()) == ChatType.DELETED) {
            throw new ChatDeletedException("Chat has been deleted");
        }

        UserEntity sender = userService.getById(senderId);

        MessageEntity message = new MessageEntity();
        message.setChatId(chatId);
        message.setSenderId(sender.getId());
        message.setContent(content);
        message.setStatus(MessageStatus.SENT);

        return messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#chatId")
    public List<MessageEntity> getByChatId(Long chatId) {
        return messageRepository.findByChatIdAndDeletedFalse(chatId);
    }

    @Transactional
    public void markAsRead(Long messageId, Long userId) {
        MessageEntity message = getById(messageId);
        ChatEntity chat = chatService.getById(message.getChatId());
        UserEntity user = userService.getById(userId);
        if (!chat.getMembers().contains(user)) {
            throw new MemberNotFoundException("You are not a member of this chat");
        }

        message.setStatus(MessageStatus.READ);
    }

    @Transactional
    @CacheEvict(key = "#id")
    public void delete(Long id) {
        MessageEntity message = getById(id);
        message.setStatus(MessageStatus.DELETED);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#chatId + '_' + #query", condition = "#query != null and #query != ''")
    public List<MessageEntity> searchInChat(Long chatId, String query) {
        return messageRepository.searchMessagesInChat(chatId, "%" + query.toLowerCase() + "%");
    }

    private MessageEntity getById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
    }
}