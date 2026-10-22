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
import ru.lakeevda.redisapp.application.command.UpdateUserCommand;
import ru.lakeevda.redisapp.application.model.PageQuery;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.model.SortOrder;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.infrastructure.security.SecurityConfig;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserUpdateRequest;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные MVC-тесты контроллера пользователей.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
@WithMockUser(username = "1", roles = "ADMIN")
class UserControllerIT {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserUseCase userUseCase;

    /**
     * Проверяет выдачу страницы пользователей.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getAllUsersReturnsPage() throws Exception {
        PageResult<User> page = new PageResult<>(List.of(user(5L)), 1, 0, 20, 1);
        when(userUseCase.getAllUsers(any(PageQuery.class))).thenReturn(page);

        mockMvc.perform(get("/api/users").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(5))
                .andExpect(jsonPath("$.content[0].email").value("user@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        ArgumentCaptor<PageQuery> captor = ArgumentCaptor.forClass(PageQuery.class);
        verify(userUseCase).getAllUsers(captor.capture());
        assertThat(captor.getValue().page()).isZero();
        assertThat(captor.getValue().size()).isEqualTo(20);
        assertThat(captor.getValue().sort())
                .containsExactly(new SortOrder("createdAt", SortOrder.Direction.ASC));
    }

    /**
     * Проверяет, что обычный пользователь может читать список пользователей.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1")
    void getAllUsersAllowedForRegularUser() throws Exception {
        when(userUseCase.getAllUsers(any(PageQuery.class)))
                .thenReturn(new PageResult<>(List.of(), 0, 0, 20, 0));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    /**
     * Проверяет получение пользователя по идентификатору.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void getUserByIdReturnsUser() throws Exception {
        when(userUseCase.getById(5L)).thenReturn(user(5L));

        mockMvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("Alice"));

        verify(userUseCase).getById(5L);
    }

    /**
     * Проверяет создание пользователя администратором.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void createReturnsCreatedUser() throws Exception {
        when(userUseCase.create(any(CreateUserCommand.class))).thenReturn(user(5L));

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UserCreateRequest(
                                "user@example.com", "Alice", "secret"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(userUseCase).create(captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("user@example.com");
        assertThat(captor.getValue().username()).isEqualTo("Alice");
        assertThat(captor.getValue().password()).isEqualTo("secret");
    }

    /**
     * Проверяет запрет создания пользователя обычным пользователем.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(username = "1")
    void createForbiddenForRegularUser() throws Exception {
        mockMvc.perform(post("/api/users")
                        .servletPath("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UserCreateRequest(
                                "user@example.com", "Alice", "secret"))))
                .andExpect(status().isForbidden());

        verify(userUseCase, never()).create(any(CreateUserCommand.class));
    }

    /**
     * Проверяет валидацию данных создания пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void createRejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UserCreateRequest(
                                "not-an-email", "Alice", "secret"))))
                .andExpect(status().isBadRequest());

        verify(userUseCase, never()).create(any(CreateUserCommand.class));
    }

    /**
     * Проверяет обновление пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void updateReturnsUpdatedUser() throws Exception {
        when(userUseCase.update(any(Long.class), any(UpdateUserCommand.class))).thenReturn(user(5L));

        mockMvc.perform(put("/api/users/5")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UserUpdateRequest("new@example.com", "Bob"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        ArgumentCaptor<UpdateUserCommand> captor = ArgumentCaptor.forClass(UpdateUserCommand.class);
        verify(userUseCase).update(org.mockito.ArgumentMatchers.eq(5L), captor.capture());
        assertThat(captor.getValue().email()).isEqualTo("new@example.com");
        assertThat(captor.getValue().username()).isEqualTo("Bob");
    }

    /**
     * Проверяет удаление пользователя.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void deleteDelegatesToUseCase() throws Exception {
        mockMvc.perform(delete("/api/users/5").with(csrf()))
                .andExpect(status().isOk());

        verify(userUseCase).delete(5L);
    }

    /**
     * Создаёт тестовую доменную модель пользователя.
     *
     * @param id идентификатор пользователя
     * @return пользователь
     */
    private User user(Long id) {
        return new User(id, "user@example.com", "Alice", "hash", CREATED_AT, UserRole.USER);
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
