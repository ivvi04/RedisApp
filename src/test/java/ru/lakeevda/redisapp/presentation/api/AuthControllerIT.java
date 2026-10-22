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
import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.exception.ResourceAlreadyExistsException;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.security.SecurityConfig;
import ru.lakeevda.redisapp.presentation.api.dto.auth.RegisterRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные MVC-тесты контроллера аутентификации.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerIT {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    /**
     * Проверяет получение данных текущего аутентифицированного пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1")
    void loginReturnsCurrentUser() throws Exception {
        when(userUseCase.getById(1L)).thenReturn(user(1L));

        mockMvc.perform(get("/api/auth/login"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"));

        verify(userUseCase).getById(1L);
    }

    /**
     * Проверяет ответ 401 для неаутентифицированного запроса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void loginReturnsUnauthorizedWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/login").servletPath("/api/auth/login"))
                .andExpect(status().isUnauthorized());

        verify(userUseCase, never()).getById(any(Long.class));
    }

    /**
     * Проверяет ответ 401, если пользователь из сессии не найден.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1")
    void loginReturnsUnauthorizedWhenUserNotFound() throws Exception {
        when(userUseCase.getById(1L)).thenThrow(new ResourceAlreadyExistsException("Не найден"));

        mockMvc.perform(get("/api/auth/login").servletPath("/api/auth/login"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет успешную регистрацию пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void registerCreatesUser() throws Exception {
        when(userUseCase.create(any(CreateUserCommand.class))).thenReturn(user(5L));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest(
                                "user@example.com", "Alice", "secret", "secret"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.username").value("Alice"));

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(userUseCase).create(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("user@example.com");
        assertThat(captor.getValue().username()).isEqualTo("Alice");
        assertThat(captor.getValue().password()).isEqualTo("secret");
    }

    /**
     * Проверяет ошибку регистрации при несовпадении паролей.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void registerRejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest(
                                "user@example.com", "Alice", "secret", "other"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пароли не совпадают"));

        verify(userUseCase, never()).create(any(CreateUserCommand.class));
    }

    /**
     * Проверяет валидацию данных регистрации.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void registerRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest(
                                "not-an-email", "Alice", "secret", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Некорректный email")));

        verify(userUseCase, never()).create(any(CreateUserCommand.class));
    }

    /**
     * Проверяет ответ при попытке зарегистрировать занятой email.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void registerReturnsBadRequestWhenEmailExists() throws Exception {
        when(userUseCase.create(any(CreateUserCommand.class)))
                .thenThrow(new ResourceAlreadyExistsException("Пользователь с таким email уже существует"));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegisterRequest(
                                "user@example.com", "Alice", "secret", "secret"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким email уже существует"));
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    private User user(Long id) {
        return new User(id, "user@example.com", "Alice", "hash",
                CREATED_AT, ru.lakeevda.redisapp.domain.enums.UserRole.USER);
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
