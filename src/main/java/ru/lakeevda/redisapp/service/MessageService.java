package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис работы с сообщениями.
 *
 * @author lakeev-da
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private static final String CACHE_MESSAGES = "messages";
    private static final String CACHE_SEARCH = "messageSearch";

    private final MessageRepository messageRepository;
    private final ChatService chatService;
    private final UserService userService;

    /**
     * Отправляет сообщение в чат.
     *
     * @param chatId   идентификатор чата
     * @param senderId идентификатор отправителя
     * @param content  текст сообщения
     * @return сохранённое сообщение
     * @throws ChatDeletedException если чат удалён
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, key = "#chatId"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public MessageResponse send(Long chatId, Long senderId, String content) {
        ChatEntity chat = chatService.getEntityById(chatId);
        if (ChatType.fromValue(chat.getType()) == ChatType.DELETED) {
            throw new ChatDeletedException("Чат был удалён");
        }

        UserEntity sender = userService.getEntityById(senderId);

        MessageEntity message = new MessageEntity();
        message.setChatId(chatId);
        message.setSenderId(sender.getId());
        message.setContent(content);
        message.setStatus(MessageStatus.SENT.getValue());

        return MessageMapper.fromEntity(messageRepository.save(message));
    }

    /**
     * Возвращает сообщения чата.
     *
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_MESSAGES, key = "#chatId", condition = "#chatId != null")
    public List<MessageResponse> getByChatId(Long chatId) {
        return messageRepository.findByChatIdAndDeletedFalse(chatId)
                .stream()
                .map(MessageMapper::fromEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Отмечает сообщение как прочитанное.
     *
     * @param id     идентификатор сообщения
     * @param userId идентификатор пользователя
     * @throws MemberNotFoundException если пользователь не является участником чата
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void markAsRead(Long id, Long userId) {
        MessageEntity message = getById(id);
        ChatEntity chat = chatService.getEntityById(message.getChatId());
        UserEntity user = userService.getEntityById(userId);
        if (!chat.getMembers().contains(user)) {
            throw new MemberNotFoundException("Вы не являетесь участником этого чата");
        }

        message.setStatus(MessageStatus.READ.name());
    }

    /**
     * Помечает сообщение как удалённое.
     *
     * @param id идентификатор сообщения
     */
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void delete(Long id) {
        MessageEntity message = getById(id);
        message.setStatus(MessageStatus.DELETED.getValue());
    }

    /**
     * Ищет сообщения внутри чата.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SEARCH,
            key = "#chatId + '_' + #query",
            condition = "#query != null and #query != ''",
            unless = "#result == null or #result.isEmpty()")
    public List<MessageResponse> searchInChat(Long chatId, String query) {
        return messageRepository.searchMessagesInChat(chatId, query.toLowerCase())
                .stream().map(MessageMapper::fromEntity)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Возвращает сущность сообщения по идентификатору.
     *
     * @param messageId идентификатор сообщения
     * @return сущность сообщения
     * @throws EntityNotFoundException если сообщение не найдено
     */
    private MessageEntity getById(Long messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException("Сообщение не найдено"));
    }
}