package ru.lakeevda.redisapp.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.application.command.SendMessageCommand;
import ru.lakeevda.redisapp.application.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.application.port.in.MessageUseCase;
import ru.lakeevda.redisapp.application.port.out.ChatRepository;
import ru.lakeevda.redisapp.application.port.out.MessageRepository;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.exception.ChatDeletedException;
import ru.lakeevda.redisapp.domain.exception.MemberNotFoundException;
import ru.lakeevda.redisapp.domain.exception.MessageAccessDeniedException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.domain.model.Message;

import java.util.List;
import java.util.Locale;

/**
 * Реализация сценариев работы с сообщениями.
 *
 * @author lakeev-da
 */
@Service
@RequiredArgsConstructor
public class MessageUseCaseImpl implements MessageUseCase {

    private static final String CACHE_MESSAGES = "messages";
    private static final String CACHE_SEARCH = "messageSearch";

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * Отправляет сообщение в чат.
     *
     * @param command команда отправки
     * @return сохранённое сообщение
     * @throws ChatDeletedException           если чат удалён
     * @throws IllegalArgumentRequestException если команда пустая
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, key = "#command.chatId()"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public Message send(SendMessageCommand command) {
        if (command == null) {
            throw new IllegalArgumentRequestException();
        }

        Chat chat = getChatOrThrow(command.chatId());
        if (chat.isDeleted()) {
            throw new ChatDeletedException("Чат был удалён");
        }

        if (command.senderId() == null || userRepository.findById(command.senderId()).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден: " + command.senderId());
        }

        return messageRepository.save(Message.sent(command.chatId(), command.senderId(), command.content()));
    }

    /**
     * Возвращает сообщения чата и отмечает прочитанными сообщения,
     * отправленные другими пользователями.
     *
     * @param chatId   идентификатор чата
     * @param readerId идентификатор читателя
     * @return список сообщений
     */
    @Override
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = CACHE_MESSAGES, key = "#chatId", condition = "#chatId != null"),
            evict = @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    )
    public List<Message> getByChatId(Long chatId, Long readerId) {
        if (readerId != null && chatRepository.isActiveMember(chatId, readerId)) {
            messageRepository.markReadByChatIdAndSenderNot(chatId, readerId);
        }

        return messageRepository.findByChatId(chatId);
    }

    /**
     * Отмечает сообщение как прочитанное.
     *
     * @param id     идентификатор сообщения
     * @param userId идентификатор пользователя
     * @throws MemberNotFoundException если пользователь не является участником чата
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void markAsRead(Long id, Long userId) {
        Message message = getMessageOrThrow(id);
        Chat chat = getChatOrThrow(message.chatId());

        if (userId == null || userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден: " + userId);
        }

        if (!chat.hasMember(userId)) {
            throw new MemberNotFoundException("Вы не являетесь участником этого чата");
        }

        if (message.senderId().equals(userId)) {
            return;
        }

        messageRepository.save(message.markRead());
    }

    /**
     * Помечает сообщение как удалённое.
     *
     * @param id      идентификатор сообщения
     * @param userId  идентификатор пользователя, удаляющего сообщение
     * @param isAdmin признак администратора
     * @throws MessageAccessDeniedException если пользователь не является автором или администратором
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_MESSAGES, allEntries = true),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true)
    })
    public void delete(Long id, Long userId, boolean isAdmin) {
        Message message = getMessageOrThrow(id);
        if (!isAdmin && !message.senderId().equals(userId)) {
            throw new MessageAccessDeniedException("Удалять сообщения может только автор или администратор");
        }

        messageRepository.save(message.markDeleted());
    }

    /**
     * Ищет сообщения внутри чата.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SEARCH,
            key = "#chatId + '_' + #query",
            condition = "#query != null and #query != ''",
            unless = "#result == null or #result.isEmpty()")
    public List<Message> searchInChat(Long chatId, String query) {
        return messageRepository.searchInChat(chatId, query.toLowerCase(Locale.ROOT));
    }

    /**
     * Возвращает сообщение или выбрасывает исключение.
     *
     * @param id идентификатор сообщения
     * @return сообщение
     * @throws ResourceNotFoundException если сообщение не найдено
     */
    private Message getMessageOrThrow(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Сообщение не найдено"));
    }

    /**
     * Возвращает чат или выбрасывает исключение.
     *
     * @param id идентификатор чата
     * @return чат
     * @throws ResourceNotFoundException если чат не найден
     */
    private Chat getChatOrThrow(Long id) {
        return chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Чат не найден"));
    }
}
