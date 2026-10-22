package ru.lakeevda.redisapp.application.usecase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.lakeevda.redisapp.application.command.CreateChatCommand;
import ru.lakeevda.redisapp.application.command.ManageChatMembersCommand;
import ru.lakeevda.redisapp.application.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.application.port.out.ChatRepository;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.ChatMemberAction;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.domain.model.User;

import java.time.Instant;
import java.util.HashSet;
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
 * Тесты бизнес-логики работы с чатами.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class ChatUseCaseImplTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatUseCaseImpl chatUseCase;

    /**
     * Проверяет получение существующего чата.
     */
    @Test
    void getByIdReturnsExistingChat() {
        Chat chat = chat(10L, ChatType.PRIVATE, Set.of(1L, 2L));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat));

        Chat result = chatUseCase.getById(10L);

        assertThat(result).isEqualTo(chat);
    }

    /**
     * Проверяет ошибку при получении несуществующего чата.
     */
    @Test
    void getByIdThrowsWhenChatNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.getById(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Чат не найден");
    }

    /**
     * Проверяет делегирование выборки чатов пользователя.
     */
    @Test
    void getUserChatsDelegatesToRepository() {
        List<Chat> chats = List.of(chat(10L, ChatType.PRIVATE, Set.of(1L, 2L)));
        when(chatRepository.findByMemberIdAndTypeNot(1L, ChatType.DELETED)).thenReturn(chats);

        List<Chat> result = chatUseCase.getUserChats(1L);

        assertThat(result).isEqualTo(chats);
        verify(chatRepository).findByMemberIdAndTypeNot(1L, ChatType.DELETED);
    }

    /**
     * Проверяет делегирование выборки всех чатов.
     */
    @Test
    void getAllChatsDelegatesToRepository() {
        List<Chat> chats = List.of(chat(10L, ChatType.PRIVATE, Set.of(1L, 2L)));
        when(chatRepository.findAllByTypeNotOrderByCreatedAtDesc(ChatType.DELETED)).thenReturn(chats);

        List<Chat> result = chatUseCase.getAllChats();

        assertThat(result).isEqualTo(chats);
        verify(chatRepository).findAllByTypeNotOrderByCreatedAtDesc(ChatType.DELETED);
    }

    /**
     * Проверяет создание чата и автоматическое добавление создателя в участники.
     */
    @Test
    void createAddsCreatorToPrivateChat() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> {
            Chat saved = invocation.getArgument(0);
            return new Chat(100L, saved.name(), saved.type(), saved.createdAt(),
                    saved.createdById(), saved.memberIds());
        });

        Chat result = chatUseCase.create(new CreateChatCommand("Team", Set.of(2L), 1L));

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        Chat saved = captor.getValue();

        assertThat(saved.id()).isNull();
        assertThat(saved.name()).isEqualTo("Team");
        assertThat(saved.createdById()).isEqualTo(1L);
        assertThat(saved.memberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(saved.type()).isEqualTo(ChatType.PRIVATE);

        assertThat(result.id()).isEqualTo(100L);
        assertThat(result.memberIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    /**
     * Проверяет, что чат с более чем двумя участниками создаётся групповым.
     */
    @Test
    void createResolvesGroupTypeForManyMembers() {
        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user(1L)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatUseCase.create(new CreateChatCommand("Group", Set.of(2L, 3L), 1L));

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(ChatType.GROUP);
        assertThat(captor.getValue().memberIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    /**
     * Проверяет создание чата без явно переданных участников.
     */
    @Test
    void createWithNullMembersAddsOnlyCreator() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatUseCase.create(new CreateChatCommand("Solo", null, 1L));

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().memberIds()).containsExactly(1L);
    }

    /**
     * Проверяет ошибку при пустой команде создания.
     */
    @Test
    void createThrowsWhenCommandIsNull() {
        assertThatThrownBy(() -> chatUseCase.create(null))
                .isInstanceOf(IllegalArgumentRequestException.class);

        verifyNoInteractions(chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку при отсутствии создателя в команде.
     */
    @Test
    void createThrowsWhenCreatorIsNull() {
        assertThatThrownBy(() -> chatUseCase.create(new CreateChatCommand("Chat", Set.of(2L), null)))
                .isInstanceOf(IllegalArgumentRequestException.class);

        verifyNoInteractions(chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку, если участник чата не найден.
     */
    @Test
    void createThrowsWhenMemberNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.create(new CreateChatCommand("Chat", Set.of(2L), 1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 2");

        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Проверяет ошибку, если создатель чата не найден.
     */
    @Test
    void createThrowsWhenCreatorNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.create(new CreateChatCommand("Chat", Set.of(), 1L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 1");

        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Проверяет добавление участника и повышение типа чата до группового.
     */
    @Test
    void manageMemberAddsUserAndUpgradesType() {
        Chat existing = chat(10L, ChatType.PRIVATE, Set.of(1L, 2L));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatUseCase.manageMember(new ManageChatMembersCommand(10L, 3L, ChatMemberAction.ADD));

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().memberIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(captor.getValue().type()).isEqualTo(ChatType.GROUP);
    }

    /**
     * Проверяет удаление участника без понижения типа группового чата.
     */
    @Test
    void manageMemberRemovesUserAndKeepsGroupType() {
        Chat existing = chat(10L, ChatType.GROUP, Set.of(1L, 2L, 3L));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userRepository.findById(3L)).thenReturn(Optional.of(user(3L)));
        when(chatRepository.save(any(Chat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        chatUseCase.manageMember(new ManageChatMembersCommand(10L, 3L, ChatMemberAction.REMOVE));

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().memberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(captor.getValue().type()).isEqualTo(ChatType.GROUP);
    }

    /**
     * Проверяет ошибку при пустой команде изменения состава.
     */
    @Test
    void manageMemberThrowsWhenCommandIsNull() {
        assertThatThrownBy(() -> chatUseCase.manageMember(null))
                .isInstanceOf(IllegalArgumentRequestException.class);

        verifyNoInteractions(chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку при отсутствии действия в команде.
     */
    @Test
    void manageMemberThrowsWhenActionIsNull() {
        assertThatThrownBy(() -> chatUseCase.manageMember(
                new ManageChatMembersCommand(10L, 3L, null)))
                .isInstanceOf(IllegalArgumentRequestException.class);

        verifyNoInteractions(chatRepository, userRepository);
    }

    /**
     * Проверяет ошибку, если чат для изменения состава не найден.
     */
    @Test
    void manageMemberThrowsWhenChatNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.manageMember(
                new ManageChatMembersCommand(10L, 3L, ChatMemberAction.ADD)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Чат не найден");

        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Проверяет ошибку, если пользователь для изменения состава не найден.
     */
    @Test
    void manageMemberThrowsWhenUserNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.of(chat(10L, ChatType.PRIVATE, Set.of(1L, 2L))));
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.manageMember(
                new ManageChatMembersCommand(10L, 3L, ChatMemberAction.ADD)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Пользователь не найден: 3");

        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Проверяет мягкое удаление чата.
     */
    @Test
    void deleteMarksChatAsDeleted() {
        Chat existing = chat(10L, ChatType.PRIVATE, Set.of(1L, 2L));
        when(chatRepository.findById(10L)).thenReturn(Optional.of(existing));

        chatUseCase.delete(10L);

        ArgumentCaptor<Chat> captor = ArgumentCaptor.forClass(Chat.class);
        verify(chatRepository).save(captor.capture());
        assertThat(captor.getValue().type()).isEqualTo(ChatType.DELETED);
    }

    /**
     * Проверяет ошибку удаления несуществующего чата.
     */
    @Test
    void deleteThrowsWhenChatNotFound() {
        when(chatRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatUseCase.delete(10L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Проверяет делегирование поиска чатов.
     */
    @Test
    void searchChatsDelegatesToRepository() {
        List<Chat> chats = List.of(chat(10L, ChatType.PRIVATE, Set.of(1L, 2L)));
        when(chatRepository.search("team", 10L)).thenReturn(chats);

        List<Chat> result = chatUseCase.searchChats("team", 10L);

        assertThat(result).isEqualTo(chats);
        verify(chatRepository).search("team", 10L);
    }

    /**
     * Создаёт тестовую доменную модель чата.
     *
     * @param id        идентификатор чата
     * @param type      тип чата
     * @param memberIds идентификаторы участников
     * @return чат
     */
    private Chat chat(Long id, ChatType type, Set<Long> memberIds) {
        return new Chat(id, "Chat", type, CREATED_AT, 1L, new HashSet<>(memberIds));
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    private User user(Long id) {
        return new User(id, "user" + id + "@example.com", "User" + id, "hash",
                CREATED_AT, ru.lakeevda.redisapp.domain.enums.UserRole.USER);
    }
}
