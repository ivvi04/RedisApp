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
import ru.lakeevda.redisapp.application.command.SendMessageCommand;
import ru.lakeevda.redisapp.application.port.in.MessageUseCase;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.enums.MessageStatus;
import ru.lakeevda.redisapp.domain.exception.MessageAccessDeniedException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;
import ru.lakeevda.redisapp.domain.model.Message;
import ru.lakeevda.redisapp.infrastructure.security.SecurityConfig;
import ru.lakeevda.redisapp.presentation.api.dto.message.MessageSendRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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
 * Интеграционные MVC-тесты контроллера сообщений.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = MessageController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "1")
class MessageControllerIT {

    private static final Instant MESSAGE_TIME = Instant.parse("2026-01-01T11:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MessageUseCase messageUseCase;

    @MockitoBean
    private UserUseCase userUseCase;

    /**
     * Проверяет отправку сообщения.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void sendReturnsSavedMessage() throws Exception {
        when(messageUseCase.send(any(SendMessageCommand.class)))
                .thenReturn(message(99L, 2L, MessageStatus.SENT));

        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new MessageSendRequest(10L, 2L, "Hello"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.chatId").value(10))
                .andExpect(jsonPath("$.senderId").value(2))
                .andExpect(jsonPath("$.content").value("Hello"))
                .andExpect(jsonPath("$.status").value("sent"));

        ArgumentCaptor<SendMessageCommand> captor = ArgumentCaptor.forClass(SendMessageCommand.class);
        verify(messageUseCase).send(captor.capture());
        assertThat(captor.getValue().chatId()).isEqualTo(10L);
        assertThat(captor.getValue().senderId()).isEqualTo(2L);
        assertThat(captor.getValue().content()).isEqualTo("Hello");
    }

    /**
     * Проверяет валидацию пустого текста сообщения.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void sendRejectsBlankContent() throws Exception {
        mockMvc.perform(post("/api/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new MessageSendRequest(10L, 2L, " "))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Введите текст сообщения")));

        verify(messageUseCase, never()).send(any(SendMessageCommand.class));
    }

    /**
     * Проверяет получение сообщений чата от имени текущего пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getByChatIdUsesAuthenticatedUserId() throws Exception {
        when(messageUseCase.getByChatId(10L, 1L))
                .thenReturn(List.of(message(1L, 2L, MessageStatus.READ)));

        mockMvc.perform(get("/api/messages/chat/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("read"));

        verify(messageUseCase).getByChatId(10L, 1L);
    }

    /**
     * Проверяет ответ 404, если чат не найден.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getByChatIdReturnsNotFound() throws Exception {
        when(messageUseCase.getByChatId(99L, 1L))
                .thenThrow(new ResourceNotFoundException("Чат не найден"));

        mockMvc.perform(get("/api/messages/chat/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Чат не найден"));
    }

    /**
     * Проверяет поиск сообщений внутри чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void searchInChatDelegatesQuery() throws Exception {
        when(messageUseCase.searchInChat(10L, "hello"))
                .thenReturn(List.of(message(1L, 2L, MessageStatus.SENT)));

        mockMvc.perform(get("/api/messages/chat/10/search").param("query", "hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(messageUseCase).searchInChat(10L, "hello");
    }

    /**
     * Проверяет отметку сообщения прочитанным.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void markAsReadDelegatesToUseCase() throws Exception {
        mockMvc.perform(put("/api/messages/99/read")
                        .with(csrf())
                        .param("userId", "2"))
                .andExpect(status().isOk());

        verify(messageUseCase).markAsRead(99L, 2L);
    }

    /**
     * Проверяет удаление сообщения обычным пользователем.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void deletePassesNonAdminFlag() throws Exception {
        mockMvc.perform(delete("/api/messages/99").with(csrf()))
                .andExpect(status().isOk());

        verify(messageUseCase).delete(99L, 1L, false);
    }

    /**
     * Проверяет удаление сообщения администратором.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1", roles = "ADMIN")
    void deletePassesAdminFlag() throws Exception {
        mockMvc.perform(delete("/api/messages/99").with(csrf()))
                .andExpect(status().isOk());

        verify(messageUseCase).delete(99L, 1L, true);
    }

    /**
     * Проверяет ответ 403 при попытке удалить чужое сообщение.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void deleteReturnsForbiddenWhenAccessDenied() throws Exception {
        org.mockito.Mockito.doThrow(new MessageAccessDeniedException(
                        "Удалять сообщения может только автор или администратор"))
                .when(messageUseCase).delete(any(Long.class), any(Long.class), anyBoolean());

        mockMvc.perform(delete("/api/messages/99")
                        .servletPath("/api/messages/99")
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Удалять сообщения может только автор или администратор"));
    }

    /**
     * Создаёт тестовую доменную модель сообщения.
     *
     * @param id     идентификатор сообщения
     * @param sender идентификатор отправителя
     * @param status статус сообщения
     * @return сообщение
     */
    private Message message(Long id, Long sender, MessageStatus status) {
        return new Message(id, sender, 10L, "Hello", MESSAGE_TIME, status);
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
