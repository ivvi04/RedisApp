package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.lakeevda.redisapp.application.port.out.MessageRepository;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.MessageEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.MessagePersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.MessageJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Адаптер хранения сообщений на основе Spring Data JPA.
 *
 * @author lakeev-da
 */
@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final MessageJpaRepository repository;
    private final MessagePersistenceMapper mapper;

    /**
     * Находит сообщение по идентификатору.
     *
     * @param id идентификатор сообщения
     * @return найденное сообщение
     */
    @Override
    public Optional<Message> findById(Long id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    /**
     * Возвращает неудалённые сообщения чата.
     *
     * @param chatId идентификатор чата
     * @return список сообщений
     */
    @Override
    public List<Message> findByChatId(Long chatId) {
        return repository.findByChatIdAndDeletedFalse(chatId)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Ищет неудалённые сообщения чата по тексту.
     *
     * @param chatId идентификатор чата
     * @param query  строка поиска
     * @return найденные сообщения
     */
    @Override
    public List<Message> searchInChat(Long chatId, String query) {
        return repository.searchMessagesInChat(chatId, query)
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Помечает непрочитанные сообщения чата как прочитанные,
     * если их отправителем не является указанный пользователь.
     *
     * @param chatId   идентификатор чата
     * @param readerId идентификатор читателя
     * @return количество обновлённых сообщений
     */
    @Override
    public int markReadByChatIdAndSenderNot(Long chatId, Long readerId) {
        return repository.markReadByChatIdAndSenderNot(chatId, readerId);
    }

    /**
     * Сохраняет сообщение.
     *
     * @param message сообщение
     * @return сохранённое сообщение
     */
    @Override
    public Message save(Message message) {
        MessageEntity entity = message.id() == null
                ? new MessageEntity()
                : repository.findById(message.id()).orElseGet(MessageEntity::new);

        mapper.apply(message, entity);
        return mapper.toDomain(repository.save(entity));
    }
}
