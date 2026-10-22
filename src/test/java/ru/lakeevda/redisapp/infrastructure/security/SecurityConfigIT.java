package ru.lakeevda.redisapp.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты цепочки фильтров Spring Security.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = SecurityConfigIT.ProtectedController.class)
@Import({SecurityConfig.class, SecurityConfigIT.ProtectedController.class})
@AutoConfigureMockMvc
class SecurityConfigIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    /**
     * Проверяет ответ 401 для неаутентифицированного API-запроса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void apiRequestWithoutAuthenticationReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/protected").servletPath("/api/protected"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Проверяет редирект на страницу входа для неаутентифицированного веб-запроса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void webRequestWithoutAuthenticationRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/protected"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }

    /**
     * Проверяет ответ 403 для API-запроса без прав администратора.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(roles = "USER")
    void apiRequestWithoutAdminRoleReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/users/protected")
                        .servletPath("/api/users/protected")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    /**
     * Проверяет редирект для веб-запроса без прав администратора.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(roles = "USER")
    void webRequestWithoutAdminRoleRedirectsToChats() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/chats"));
    }

    /**
     * Проверяет доступ администратора к защищённому API.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessProtectedApi() throws Exception {
        mockMvc.perform(get("/api/protected").servletPath("/api/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    /**
     * Проверяет доступ администратора к административному API.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanAccessAdminApi() throws Exception {
        mockMvc.perform(post("/api/users/protected")
                        .servletPath("/api/users/protected")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    /**
     * Проверяет редирект администратора на главную после входа.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void adminLoginRedirectsToRoot() throws Exception {
        prepareLogin("ADMIN");

        mockMvc.perform(formLogin("/login")
                        .user("email", "admin@example.com")
                        .password("password", "secret"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    /**
     * Проверяет редирект обычного пользователя на список чатов после входа.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void userLoginRedirectsToChats() throws Exception {
        prepareLogin("USER");

        mockMvc.perform(formLogin("/login")
                        .user("email", "user@example.com")
                        .password("password", "secret"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/chats"));
    }

    /**
     * Настраивает успешную аутентификацию для указанной роли.
     *
     * @param role роль пользователя
     */
    private void prepareLogin(String role) {
        String email = role.toLowerCase() + "@example.com";
        when(userDetailsService.loadUserByUsername(email))
                .thenReturn(org.springframework.security.core.userdetails.User
                        .withUsername(email)
                        .password("hash")
                        .roles(role)
                        .build());
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
    }

    /**
     * Тестовый контроллер защищённых ресурсов.
     */
    @RestController
    public static class ProtectedController {

        /**
         * Защищённый API-ресурс.
         *
         * @return успешный ответ
         */
        @GetMapping("/api/protected")
        public String api() {
            return "ok";
        }

        /**
         * Защищённый веб-ресурс.
         *
         * @return успешный ответ
         */
        @GetMapping("/protected")
        public String page() {
            return "ok";
        }

        /**
         * Главная страница, доступная только администратору.
         *
         * @return успешный ответ
         */
        @GetMapping("/")
        public String root() {
            return "ok";
        }

        /**
         * Административный API-ресурс.
         *
         * @return успешный ответ
         */
        @PostMapping("/api/users/protected")
        public String adminApi() {
            return "ok";
        }
    }
}
