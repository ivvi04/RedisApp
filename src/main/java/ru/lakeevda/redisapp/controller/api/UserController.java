package ru.lakeevda.redisapp.controller.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.dto.user.UserUpdateRequest;
import ru.lakeevda.redisapp.service.UserService;

/**
 * REST-контроллер для управления пользователями.
 *
 * @author lakeev-da
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    /**
     * Создаёт контроллер с сервисом пользователей.
     *
     * @param userService сервис пользователей
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Возвращает страницу пользователей.
     *
     * @param pageable параметры постраничной выборки
     * @return страница пользователей
     */
    @GetMapping
    public Page<UserResponse> getAllUsers(
            @PageableDefault(sort = "createdAt") Pageable pageable) {
        return userService.getAllUsers(pageable);
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     */
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    /**
     * Создаёт пользователя.
     *
     * @param request данные нового пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return userService.create(request);
    }

    /**
     * Обновляет пользователя.
     *
     * @param userId  идентификатор пользователя
     * @param request новые данные пользователя
     * @return обновлённый пользователь
     */
    @PutMapping("/{userId}")
    public UserResponse update(@PathVariable Long userId, @Valid @RequestBody UserUpdateRequest request) {
        return userService.update(userId, request);
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
