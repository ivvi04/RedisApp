package ru.lakeevda.redisapp.infrastructure.adapter.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.infrastructure.persistence.entity.MessageEntity;
import ru.lakeevda.redisapp.infrastructure.persistence.mapper.MessagePersistenceMapper;
import ru.lakeevda.redisapp.infrastructure.persistence.repository.MessageJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты адаптера хранения сообщений.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class MessageRepositoryImplTest {

    private static final Instant SENT_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private MessageJpaRepository repository;

    private MessageRepositoryImpl adapter;

    /**
     * Создаёт тестируемый адаптер с реальным маппером.
     */
    @BeforeEach
    void setUp() {
        adapter = new MessageRepositoryImpl(repository, new MessagePersistenceMapper());
    }

    /**
     * Проверяет поиск сообщения по идентификатору.
     */
    @Test
    void findByIdMapsEntityToDomain() {
        when(repository.findById(3L)).thenReturn(Optional.of(entity(3L)));

        Optional<Message> result = adapter.findById(3L);

        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(3L);
        assertThat(result.get().chatId()).isEqualTo(10L);
        assertThat(result.get().senderId()).isEqualTo(1L);
        assertThat(result.get().status()).isEqualTo(MessageStatus.READ);
    }

    /**
     * Проверяет пустой результат поиска по идентификатору.
     */
    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        when(repository.findById(3L)).thenReturn(Optional.empty());

        assertThat(adapter.findById(3L)).isEmpty();
    }

    /**
     * Проверяет выборку неудалённых сообщений чата.
     */
    @Test
    void findByChatIdMapsEntities() {
        when(repository.findByChatIdAndDeletedFalse(10L)).thenReturn(List.of(entity(3L)));

        List<Message> result = adapter.findByChatId(10L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(3L);
        verify(repository).findByChatIdAndDeletedFalse(10L);
    }

    /**
     * Проверяет поиск сообщений по тексту.
     */
    @Test
    void searchInChatMapsEntities() {
        when(repository.searchMessagesInChat(10L, "hello")).thenReturn(List.of(entity(3L)));

        List<Message> result = adapter.searchInChat(10L, "hello");

        assertThat(result).hasSize(1);
        verify(repository).searchMessagesInChat(10L, "hello");
    }

    /**
     * Проверяет делегирование отметки сообщений прочитанными.
     */
    @Test
    void markReadByChatIdAndSenderNotDelegatesToRepository() {
        when(repository.markReadByChatIdAndSenderNot(10L, 1L)).thenReturn(2);

        int result = adapter.markReadByChatIdAndSenderNot(10L, 1L);

        assertThat(result).isEqualTo(2);
        verify(repository).markReadByChatIdAndSenderNot(10L, 1L);
    }

    /**
     * Проверяет сохранение нового сообщения.
     */
    @Test
    void saveCreatesNewEntity() {
        when(repository.save(any(MessageEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Message message = new Message(null, 1L, 10L, "hello", SENT_AT, MessageStatus.SENT);
        Message result = adapter.save(message);

        ArgumentCaptor<MessageEntity> captor = ArgumentCaptor.forClass(MessageEntity.class);
        verify(repository).save(captor.capture());
        MessageEntity saved = captor.getValue();

        assertThat(saved.getSenderId()).isEqualTo(1L);
        assertThat(saved.getChatId()).isEqualTo(10L);
        assertThat(saved.getContent()).isEqualTo("hello");
        assertThat(saved.getTimestamp()).isEqualTo(SENT_AT);
        assertThat(saved.getStatus()).isEqualTo("sent");
        assertThat(result.id()).isNull();
        verify(repository, never()).findById(any(Long.class));
    }

    /**
     * Проверяет обновление существующего сообщения.
     */
    @Test
    void saveUpdatesExistingEntity() {
        MessageEntity existing = entity(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        Message message = new Message(3L, 1L, 10L, "updated", SENT_AT, MessageStatus.DELETED);
        Message result = adapter.save(message);

        assertThat(result.id()).isEqualTo(3L);
        assertThat(existing.getContent()).isEqualTo("updated");
        assertThat(existing.getStatus()).isEqualTo("deleted");
        verify(repository).findById(3L);
        verify(repository).save(existing);
    }

    /**
     * Создаёт JPA-сущность сообщения.
     *
     * @param id идентификатор сообщения
     * @return сущность сообщения
     */
    private MessageEntity entity(Long id) {
        MessageEntity entity = new MessageEntity();
        entity.setId(id);
        entity.setSenderId(1L);
        entity.setChatId(10L);
        entity.setContent("hello");
        entity.setTimestamp(SENT_AT);
        entity.setStatus("read");
        return entity;
    }
}
