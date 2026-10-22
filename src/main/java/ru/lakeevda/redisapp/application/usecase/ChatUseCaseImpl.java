package ru.lakeevda.redisapp.application.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.application.command.CreateChatCommand;
import ru.lakeevda.redisapp.application.command.ManageChatMembersCommand;
import ru.lakeevda.redisapp.application.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.application.port.in.ChatUseCase;
import ru.lakeevda.redisapp.application.port.out.ChatRepository;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Chat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Реализация сценариев работы с чатами.
 *
 * @author lakeev-da
 */
@Service
@RequiredArgsConstructor
public class ChatUseCaseImpl implements ChatUseCase {

    private static final String CACHE_CHATS = "chats";
    private static final String CACHE_SEARCH = "chatSearch";
    private static final String CACHE_USER_CHATS = "userChats";

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * Возвращает чат по идентификатору.
     *
     * @param id идентификатор чата
     * @return чат
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_CHATS, key = "#id", unless = "#result == null")
    public Chat getById(Long id) {
        return getChatOrThrow(id);
    }

    /**
     * Возвращает чаты пользователя, в которых он является участником.
     *
     * @param userId идентификатор пользователя
     * @return список чатов
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_USER_CHATS, key = "#userId")
    public List<Chat> getUserChats(Long userId) {
        return chatRepository.findByMemberIdAndTypeNot(userId, ChatType.DELETED);
    }

    /**
     * Возвращает все неудалённые чаты.
     *
     * @return список всех чатов
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_USER_CHATS, key = "'all'")
    public List<Chat> getAllChats() {
        return chatRepository.findAllByTypeNotOrderByCreatedAtDesc(ChatType.DELETED);
    }

    /**
     * Создаёт новый чат и автоматически добавляет создателя в участники.
     *
     * @param command команда создания
     * @return созданный чат
     * @throws IllegalArgumentRequestException если команда пустая
     */
    @Override
    @Transactional
    @Caching(
            put = @CachePut(cacheNames = CACHE_CHATS, key = "#result.id()"),
            evict = @CacheEvict(cacheNames = CACHE_USER_CHATS, allEntries = true)
    )
    public Chat create(CreateChatCommand command) {
        if (command == null || command.createdById() == null) {
            throw new IllegalArgumentRequestException();
        }

        Set<Long> memberIds = command.memberIds() == null
                ? new HashSet<>()
                : new HashSet<>(command.memberIds());

        memberIds.forEach(this::ensureUserExists);
        ensureUserExists(command.createdById());

        Chat chat = Chat.create(command.name(), command.createdById(), memberIds);
        return chatRepository.save(chat);
    }

    /**
     * Добавляет или удаляет участника чата и обновляет тип чата при необходимости.
     *
     * @param command команда изменения состава
     * @throws IllegalArgumentRequestException если команда пустая
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_CHATS, key = "#command.chatId()"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CACHE_USER_CHATS, allEntries = true)
    })
    public void manageMember(ManageChatMembersCommand command) {
        if (command == null || command.action() == null) {
            throw new IllegalArgumentRequestException();
        }

        Chat chat = getChatOrThrow(command.chatId());
        ensureUserExists(command.userId());

        Chat updated = switch (command.action()) {
            case ADD -> chat.addMember(command.userId());
            case REMOVE -> chat.removeMember(command.userId());
        };

        chatRepository.save(updated.withActualType());
    }

    /**
     * Помечает чат как удалённый.
     *
     * @param id идентификатор чата
     */
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_CHATS, key = "#id"),
            @CacheEvict(cacheNames = CACHE_SEARCH, allEntries = true),
            @CacheEvict(cacheNames = CACHE_USER_CHATS, allEntries = true)
    })
    public void delete(Long id) {
        Chat chat = getChatOrThrow(id);
        chatRepository.save(chat.markDeleted());
    }

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CACHE_SEARCH,
            key = "#query + '_' + #chatId",
            condition = "#query != null and #query != ''",
            unless = "#result == null or #result.isEmpty()")
    public List<Chat> searchChats(String query, Long chatId) {
        return chatRepository.search(query, chatId);
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

    /**
     * Проверяет существование пользователя.
     *
     * @param userId идентификатор пользователя
     * @throws ResourceNotFoundException если пользователь не найден
     */
    private void ensureUserExists(Long userId) {
        if (userId == null || userRepository.findById(userId).isEmpty()) {
            throw new ResourceNotFoundException("Пользователь не найден: " + userId);
        }
    }
}
