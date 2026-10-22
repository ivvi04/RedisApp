package ru.lakeevda.redisapp.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.command.UpdateUserCommand;
import ru.lakeevda.redisapp.application.model.PageResult;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.domain.model.User;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserResponse;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserUpdateRequest;
import ru.lakeevda.redisapp.presentation.api.mapper.PageQueryMapper;
import ru.lakeevda.redisapp.presentation.api.mapper.PageResponseMapper;
import ru.lakeevda.redisapp.presentation.api.mapper.UserResponseMapper;

import java.util.List;

/**
 * REST-контроллер для управления пользователями.
 *
 * @author lakeev-da
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    /**
     * Возвращает страницу пользователей.
     *
     * @param pageable параметры постраничной выборки
     * @return страница пользователей
     */
    @GetMapping
    public Page<UserResponse> getAllUsers(
            @PageableDefault(sort = "createdAt") Pageable pageable) {

        PageResult<User> result = userUseCase.getAllUsers(PageQueryMapper.fromPageable(pageable));
        List<UserResponse> content = result.content().stream()
                .map(UserResponseMapper::fromDomain)
                .toList();

        return PageResponseMapper.toPage(content, pageable, result.totalElements());
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return данные пользователя
     */
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        return UserResponseMapper.fromDomain(userUseCase.getById(userId));
    }

    /**
     * Создаёт пользователя.
     *
     * @param request данные нового пользователя
     * @return созданный пользователь
     */
    @PostMapping
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return UserResponseMapper.fromDomain(userUseCase.create(new CreateUserCommand(
                request.email(),
                request.username(),
                request.password())));
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
        return UserResponseMapper.fromDomain(userUseCase.update(userId, new UpdateUserCommand(
                request.email(),
                request.username())));
    }

    /**
     * Удаляет пользователя.
     *
     * @param userId идентификатор пользователя
     */
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        userUseCase.delete(userId);
    }
}
