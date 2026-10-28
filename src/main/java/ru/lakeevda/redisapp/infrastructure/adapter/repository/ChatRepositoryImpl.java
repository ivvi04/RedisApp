package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.application.port.out.ChatRepository;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.ChatEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.UserEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.ChatPersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.ChatJpaRepository;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.UserJpaRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Адаптер хранения чатов на основе Spring Data JPA.
 *
 * @author lakeev-da
 */
@Repository
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatRepository {

    private final ChatJpaRepository chatRepository;
    private final UserJpaRepository userRepository;
    private final ChatPersistenceMapper mapper;

    /**
     * Находит чат по идентификатору.
     *
     * @param id идентификатор чата
     * @return найденный чат
     */
    @Override
    public Optional<Chat> findById(Long id) {
        return chatRepository.findById(id).map(mapper::toDomain);
    }

    /**
     * Проверяет, что пользователь состоит в неудалённом чате.
     *
     * @param chatId идентификатор чата
     * @param userId идентификатор пользователя
     * @return {@code true}, если пользователь является участником активного чата
     */
    @Override
    public boolean isActiveMember(Long chatId, Long userId) {
        return chatRepository.existsActiveMember(chatId, userId);
    }

    /**
     * Возвращает чаты участника, исключая указанный тип.
     *
     * @param userId       идентификатор пользователя
     * @param excludedType исключаемый тип
     * @return список чатов
     */
    @Override
    public List<Chat> findByMemberIdAndTypeNot(Long userId, ChatType excludedType) {
        return chatRepository.findByMemberIdAndTypeNot(userId, excludedType.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Возвращает чаты, кроме указанного типа, от новых к старым.
     *
     * @param excludedType исключаемый тип
     * @return список чатов
     */
    @Override
    public List<Chat> findAllByTypeNotOrderByCreatedAtDesc(ChatType excludedType) {
        return chatRepository.findByTypeNotOrderByCreatedAtDesc(excludedType.getValue())
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Ищет чаты по названию или идентификатору.
     *
     * @param query  строка поиска
     * @param chatId идентификатор чата
     * @return найденные чаты
     */
    @Override
    public List<Chat> search(String query, Long chatId) {
        return chatRepository.searchChats(query, chatId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Сохраняет чат.
     *
     * @param chat чат
     * @return сохранённый чат
     */
    @Override
    public Chat save(Chat chat) {
        ChatEntity entity = chat.id() == null
                ? new ChatEntity()
                : chatRepository.findById(chat.id()).orElseGet(ChatEntity::new);

        Set<UserEntity> members = chat.memberIds().stream()
                .map(userRepository::getReferenceById)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        mapper.apply(chat, entity, members);
        return mapper.toDomain(chatRepository.save(entity));
    }
}
