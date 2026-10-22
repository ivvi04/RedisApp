package ru.lakeevda.redisapp.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lakeevda.redisapp.application.command.SendMessageCommand;
import ru.lakeevda.redisapp.application.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.application.port.out.ChatRepository;
import ru.lakeevda.redisapp.application.port.out.MessageRepository;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.exception.ChatDeletedException;
import ru.lakeevda.redisapp.domain.exception.MemberNotFoundException;
import ru.lakeevda.redisapp.domain.exception.MessageAccessDeniedException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.domain.model.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Тесты бизнес-логики работы с сообщениями.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class MessageUseCaseImplTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    private static final Instant MESSAGE_TIME = Instant.parse("2026-01-01T11:00:00Z");

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageUseCaseImpl messageUseCase;

    /**
     * Проверяет успешную отправку сообщения.
     */
    @Test
    void sendSavesNewMessage() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> {
            Message saved = invocation.getArgument(0);
            return new Message(99L, saved.senderId(), saved.chatId(), saved.content(),
                    saved.timestamp(), saved.status());
        });

        Message result = messageUseCase.send(new SendMessageCommand(10L, 2L, "Hello"));

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message saved = captor.getValue();

        assertThat(saved.id()).isNull();
        assertThat(saved.chatId()).isEqualTo(10L);
        assertThat(saved.senderId()).isEqualTo(2L);
        assertThat(saved.content()).isEqualTo("Hello");
        assertThat(saved.status()).isEqualTo(MessageStatus.SENT);
        assertThat(saved.timestamp()).isNotNull();

        assertThat(result.id()).isEqualTo(99L);
    }

    /**
     * Проверяет ошибку при пустой команде отправки.
     */
    @Test
    void sendThrowsWhenCommandIsNull() {
        assertThatThrownBy(() -> messageUseCase.send(null))
                .isInstanceOf(IllegalArgumentRequestException.class);

        verifyNoInteractions(messageRepository, chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку отправки в несуществующий чат.
     */
    @Test
    void sendThrowsWhenChatNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.send(new SendMessageCommand(10L, 2L, "Hello")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Чат не найден");

        verifyNoInteractions(messageRepository);
    }

    /**
     * Проверяет запрет отправки в удалённый чат.
     */
    @Test
    void sendThrowsWhenChatIsDeleted() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.DELETED, CREATED_AT, 1L, Set.of(1L, 2L))));

        assertThatThrownBy(() -> messageUseCase.send(new SendMessageCommand(10L, 2L, "Hello")))
                .isInstanceOf(ChatDeletedException.class)
                .hasMessageContaining("Чат был удалён");

        verifyNoInteractions(messageRepository, userRepository);
    }

    /**
     * Проверяет ошибку отправки без отправителя.
     */
    @Test
    void sendThrowsWhenSenderIsNull() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));

        assertThatThrownBy(() -> messageUseCase.send(new SendMessageCommand(10L, null, "Hello")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: null");

        verifyNoInteractions(messageRepository);
    }

    /**
     * Проверяет ошибку отправки от несуществующего пользователя.
     */
    @Test
    void sendThrowsWhenSenderNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.send(new SendMessageCommand(10L, 2L, "Hello")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 2");

        verifyNoInteractions(messageRepository);
    }

    /**
     * Проверяет автоматическое прочтение чужих сообщений при получении истории чата.
     */
    @Test
    void getByChatIdMarksOtherMessagesAsRead() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        List<Message> messages = List.of(message(1L, 1L, MessageStatus.SENT));
        when(messageRepository.findByChatId(10L)).thenReturn(messages);

        List<Message> result = messageUseCase.getByChatId(10L, 2L);

        assertThat(result).isEqualTo(messages);
        verify(messageRepository).markReadByChatIdAndSenderNot(10L, 2L);
        verify(messageRepository).findByChatId(10L);
    }

    /**
     * Проверяет, что без читателя история не меняет статусы сообщений.
     */
    @Test
    void getByChatIdWithoutReaderDoesNotMarkMessages() {
        List<Message> messages = List.of(message(1L, 1L, MessageStatus.SENT));
        when(messageRepository.findByChatId(10L)).thenReturn(messages);

        List<Message> result = messageUseCase.getByChatId(10L, null);

        assertThat(result).isEqualTo(messages);
        verify(messageRepository, never()).markReadByChatIdAndSenderNot(any(Long.class), any(Long.class));
        verifyNoInteractions(chatRepository);
    }

    /**
     * Проверяет, что удалённый чат не приводит к прочтению сообщений.
     */
    @Test
    void getByChatIdDoesNotMarkMessagesInDeletedChat() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.DELETED, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(messageRepository.findByChatId(10L)).thenReturn(List.of());

        messageUseCase.getByChatId(10L, 2L);

        verify(messageRepository, never()).markReadByChatIdAndSenderNot(any(Long.class), any(Long.class));
    }

    /**
     * Проверяет, что сообщения не отмечаются прочитанными пользователем вне чата.
     */
    @Test
    void getByChatIdDoesNotMarkMessagesForNonMember() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(messageRepository.findByChatId(10L)).thenReturn(List.of());

        messageUseCase.getByChatId(10L, 3L);

        verify(messageRepository, never()).markReadByChatIdAndSenderNot(any(Long.class), any(Long.class));
    }

    /**
     * Проверяет ошибку при отметке несуществующего сообщения.
     */
    @Test
    void markAsReadThrowsWhenMessageNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.markAsRead(99L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Сообщение не найдено");

        verifyNoInteractions(chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку при отсутствии чата сообщения.
     */
    @Test
    void markAsReadThrowsWhenChatNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.markAsRead(99L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Чат не найден");

        verifyNoInteractions(userRepository);
    }

    /**
     * Проверяет ошибку при отметке сообщения несуществующим пользователем.
     */
    @Test
    void markAsReadThrowsWhenUserNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.markAsRead(99L, 2L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 2");
    }

    /**
     * Проверяет запрет отметки сообщения пользователем вне чата.
     */
    @Test
    void markAsReadThrowsWhenUserIsNotMember() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L)));

        assertThatThrownBy(() -> messageUseCase.markAsRead(99L, 3L))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("не являетесь участником");

        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Проверяет, что автор не меняет статус собственного сообщения.
     */
    @Test
    void markAsReadIgnoresOwnMessage() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 2L, MessageStatus.SENT)));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));

        messageUseCase.markAsRead(99L, 2L);

        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Проверяет отметку чужого сообщения прочитанным.
     */
    @Test
    void markAsReadSavesReadStatus() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(
                new Chat(10L, "Chat", ChatType.PRIVATE, CREATED_AT, 1L, Set.of(1L, 2L))));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        messageUseCase.markAsRead(99L, 2L);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(MessageStatus.READ);
    }

    /**
     * Проверяет ошибку удаления несуществующего сообщения.
     */
    @Test
    void deleteThrowsWhenMessageNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageUseCase.delete(99L, 2L, false))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Сообщение не найдено");

        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Проверяет запрет удаления чужого сообщения обычным пользователем.
     */
    @Test
    void deleteThrowsWhenUserIsNotAuthorAndNotAdmin() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));

        assertThatThrownBy(() -> messageUseCase.delete(99L, 2L, false))
                .isInstanceOf(MessageAccessDeniedException.class)
                .hasMessageContaining("только автор или администратор");

        verify(messageRepository, never()).save(any(Message.class));
    }

    /**
     * Проверяет удаление сообщения автором.
     */
    @Test
    void deleteByAuthorMarksMessageAsDeleted() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 2L, MessageStatus.SENT)));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        messageUseCase.delete(99L, 2L, false);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(MessageStatus.DELETED);
    }

    /**
     * Проверяет удаление чужого сообщения администратором.
     */
    @Test
    void deleteByAdminMarksMessageAsDeleted() {
        when(messageRepository.findById(99L)).thenReturn(Optional.of(message(99L, 1L, MessageStatus.SENT)));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        messageUseCase.delete(99L, 2L, true);

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(MessageStatus.DELETED);
    }

    /**
     * Проверяет приведение поискового запроса к нижнему регистру.
     */
    @Test
    void searchInChatLowercasesQuery() {
        List<Message> messages = List.of(message(1L, 1L, MessageStatus.SENT));
        when(messageRepository.searchInChat(10L, "hello")).thenReturn(messages);

        List<Message> result = messageUseCase.searchInChat(10L, "HELLO");

        assertThat(result).isEqualTo(messages);
        verify(messageRepository).searchInChat(10L, "hello");
    }

    /**
     * Создаёт тестовую доменную модель сообщения.
     *
     * @param id      идентификатор сообщения
     * @param sender  идентификатор отправителя
     * @param status  статус сообщения
     * @return сообщение
     */
    private Message message(Long id, Long sender, MessageStatus status) {
        return new Message(id, sender, 10L, "Hello", MESSAGE_TIME, status);
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    private User user(Long id) {
        return new User(id, "user" + id + "@example.com", "User" + id, "hash", CREATED_AT, UserRole.USER);
    }
}
