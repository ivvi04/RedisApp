package ru.lakeevda.redisapp.presentation.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.exception.ChatDeletedException;
import ru.lakeevda.redisapp.domain.exception.MemberNotFoundException;
import ru.lakeevda.redisapp.domain.exception.MessageAccessDeniedException;
import ru.lakeevda.redisapp.domain.exception.ResourceAlreadyExistsException;
import ru.lakeevda.redisapp.domain.exception.ResourceNotFoundException;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные MVC-тесты глобального обработчика исключений.
 *
 * @author lakeev-da
 */
@WebMvcTest(controllers = AdviceControllerIT.ExceptionThrowingController.class)
@Import({AdviceController.class, AdviceControllerIT.ExceptionThrowingController.class})
@AutoConfigureMockMvc(addFilters = false)
class AdviceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserUseCase userUseCase;

    /**
     * Проверяет обработку некорректного аргумента.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void illegalArgumentReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректный аргумент"));
    }

    /**
     * Проверяет обработку ошибок валидации тела запроса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void invalidBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test/validated-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Укажите имя")));
    }

    /**
     * Проверяет обработку ошибок валидации параметров метода.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void invalidMethodParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/method-validation").param("value", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректные параметры запроса"));
    }

    /**
     * Проверяет обработку нарушения ограничений валидации.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void constraintViolationReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректные параметры запроса"));
    }

    /**
     * Проверяет обработку нечитаемого тела запроса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void unreadableBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/test/body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Некорректное тело запроса"));
    }

    /**
     * Проверяет обработку попытки создать существующий ресурс.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void resourceAlreadyExistsReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/resource-exists"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Ресурс уже существует"));
    }

    /**
     * Проверяет обработку обращения к удалённому чату.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void chatDeletedReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/test/chat-deleted"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Чат был удалён"));
    }

    /**
     * Проверяет обработку отсутствующего ресурса.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void resourceNotFoundReturnsNotFound() throws Exception {
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Ресурс не найден"));
    }

    /**
     * Проверяет обработку отсутствующего участника чата.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void memberNotFoundReturnsNotFound() throws Exception {
        mockMvc.perform(get("/test/member-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Участник не найден"));
    }

    /**
     * Проверяет обработку запрета доступа к сообщению.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void messageAccessDeniedReturnsForbidden() throws Exception {
        mockMvc.perform(get("/test/message-access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Доступ запрещён"));
    }

    /**
     * Проверяет обработку непредвиденного исключения.
     *
     * @throws Exception при ошибке выполнения запроса
     */
    @Test
    void unexpectedExceptionReturnsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Внутренняя ошибка сервера"));
    }

    /**
     * Тестовый контроллер, выбрасывающий исключения для проверки обработчика.
     */
    @RestController
    @RequestMapping("/test")
    public static class ExceptionThrowingController {

        /**
         * Выбрасывает {@link IllegalArgumentException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/illegal-argument")
        public String illegalArgument() {
            throw new IllegalArgumentException("Некорректный аргумент");
        }

        /**
         * Принимает тело с обязательным полем.
         *
         * @param body тело запроса
         * @return успешный ответ
         */
        @PostMapping("/validated-body")
        public String validatedBody(@Valid @RequestBody ValidatedBody body) {
            return body.name();
        }

        /**
         * Принимает параметр с ограничением.
         *
         * @param value значение параметра
         * @return успешный ответ
         */
        @GetMapping("/method-validation")
        public String methodValidation(@RequestParam @Min(1) int value) {
            return String.valueOf(value);
        }

        /**
         * Выбрасывает {@link ConstraintViolationException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/constraint-violation")
        public String constraintViolation() {
            throw new ConstraintViolationException(Set.<ConstraintViolation<?>>of());
        }

        /**
         * Принимает тело запроса без валидации.
         *
         * @param body тело запроса
         * @return успешный ответ
         */
        @PostMapping("/body")
        public String body(@RequestBody ValidatedBody body) {
            return body.name();
        }

        /**
         * Выбрасывает {@link ResourceAlreadyExistsException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/resource-exists")
        public String resourceExists() {
            throw new ResourceAlreadyExistsException("Ресурс уже существует");
        }

        /**
         * Выбрасывает {@link ChatDeletedException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/chat-deleted")
        public String chatDeleted() {
            throw new ChatDeletedException("Чат был удалён");
        }

        /**
         * Выбрасывает {@link ResourceNotFoundException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/resource-not-found")
        public String resourceNotFound() {
            throw new ResourceNotFoundException("Ресурс не найден");
        }

        /**
         * Выбрасывает {@link MemberNotFoundException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/member-not-found")
        public String memberNotFound() {
            throw new MemberNotFoundException("Участник не найден");
        }

        /**
         * Выбрасывает {@link MessageAccessDeniedException}.
         *
         * @return не возвращает значение
         */
        @GetMapping("/message-access-denied")
        public String messageAccessDenied() {
            throw new MessageAccessDeniedException("Доступ запрещён");
        }

        /**
         * Выбрасывает непредвиденное исключение.
         *
         * @return не возвращает значение
         */
        @GetMapping("/unexpected")
        public String unexpected() {
            throw new IllegalStateException("Неожиданная ошибка");
        }
    }

    /**
     * Тело запроса для проверки валидации.
     *
     * @param name имя
     */
    public record ValidatedBody(@NotBlank(message = "Укажите имя") String name) {
    }
}
