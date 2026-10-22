package ru.lakeevda.redisapp.presentation.api;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.lakeevda.redisapp.application.command.CreateChatCommand;
import ru.lakeevda.redisapp.application.command.ManageChatMembersCommand;
import ru.lakeevda.redisapp.application.port.in.ChatUseCase;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.enums.ChatMemberAction;
import ru.lakeevda.redisapp.domain.enums.ChatType;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Chat;
import ru.lakeevda.redisapp.infrastructure.security.SecurityConfig;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatCreateRequest;
import ru.lakeevda.redisapp.presentation.api.dto.chat.ChatManageMembersRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные MVC-тесты контроллера чатов.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = ChatController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "1")
class ChatControllerIT {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatUseCase chatUseCase;

    @MockitoBean
    private UserUseCase userUseCase;

    /**
     * Проверяет выдачу чатов текущего пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getMyChatsReturnsUserChats() throws Exception {
        when(chatUseCase.getUserChats(1L)).thenReturn(List.of(chat()));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Team"))
                .andExpect(jsonPath("$[0].type").value("private"))
                .andExpect(jsonPath("$[0].memberIds.length()").value(2));

        verify(chatUseCase).getUserChats(1L);
        verify(chatUseCase, never()).getAllChats();
    }

    /**
     * Проверяет, что администратор получает все чаты.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void getMyChatsReturnsAllChatsForAdmin() throws Exception {
        when(chatUseCase.getAllChats()).thenReturn(List.of(chat()));

        mockMvc.perform(get("/api/chats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(chatUseCase).getAllChats();
        verify(chatUseCase, never()).getUserChats(any(Long.class));
    }

    /**
     * Проверяет создание чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void createReturnsCreatedChat() throws Exception {
        when(chatUseCase.create(any(CreateChatCommand.class))).thenReturn(chat());

        mockMvc.perform(post("/api/chats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChatCreateRequest("Team", Set.of(1L, 2L), 1L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Team"));

        ArgumentCaptor<CreateChatCommand> captor = ArgumentCaptor.forClass(CreateChatCommand.class);
        verify(chatUseCase).create(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Team");
        assertThat(captor.getValue().memberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(captor.getValue().createdById()).isEqualTo(1L);
    }

    /**
     * Проверяет валидацию обязательных полей при создании чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void createRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/chats")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChatCreateRequest("Team", Set.of(1L, 2L), null))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Укажите создателя чата")));

        verify(chatUseCase, never()).create(any(CreateChatCommand.class));
    }

    /**
     * Проверяет получение чата по идентификатору.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getByIdReturnsChat() throws Exception {
        when(chatUseCase.getById(10L)).thenReturn(chat());

        mockMvc.perform(get("/api/chats/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    /**
     * Проверяет ответ 404 для несуществующего чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getByIdReturnsNotFound() throws Exception {
        when(chatUseCase.getById(99L)).thenThrow(new ResourceNotFoundException("Чат не найден"));

        mockMvc.perform(get("/api/chats/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Чат не найден"));
    }

    /**
     * Проверяет делегирование поиска чатов.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void searchChatsReturnsFoundChats() throws Exception {
        when(chatUseCase.searchChats("Team", 10L)).thenReturn(List.of(chat()));

        mockMvc.perform(get("/api/chats/search")
                        .param("query", "Team")
                        .param("chatId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));

        verify(chatUseCase).searchChats("Team", 10L);
    }

    /**
     * Проверяет добавление участника чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void manageMembersAddsMember() throws Exception {
        mockMvc.perform(put("/api/chats/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChatManageMembersRequest(10L, 3L, "ADD"))))
                .andExpect(status().isOk());

        verify(chatUseCase).manageMember(new ManageChatMembersCommand(10L, 3L, ChatMemberAction.ADD));
    }

    /**
     * Проверяет валидацию действия над участником.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void manageMembersRejectsInvalidAction() throws Exception {
        mockMvc.perform(put("/api/chats/members")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChatManageMembersRequest(10L, 3L, "UNKNOWN"))))
                .andExpect(status().isBadRequest());

        verify(chatUseCase, never()).manageMember(any(ManageChatMembersCommand.class));
    }

    /**
     * Проверяет удаление чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void deleteMarksChatAsDeleted() throws Exception {
        mockMvc.perform(delete("/api/chats/10").with(csrf()))
                .andExpect(status().isOk());

        verify(chatUseCase).delete(10L);
    }

    /**
     * Создаёт тестовую доменную модель чата.
     *
     * @return чат
     */
    private Chat chat() {
        return new Chat(10L, "Team", ChatType.PRIVATE, CREATED_AT, 1L,
                new HashSet<>(Set.of(1L, 2L)));
    }

    /**
     * Сериализует объект запроса в JSON.
     *
     * @param value объект запроса
     * @return JSON-представление
     */
    private String json(Object value) {
        return objectMapper.writeValueAsString(value);
    }
}
