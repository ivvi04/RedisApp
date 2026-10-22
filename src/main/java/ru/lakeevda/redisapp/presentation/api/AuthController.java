package ru.lakeevda.redisapp.presentation.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lakeevda.redisapp.application.command.CreateUserCommand;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.presentation.api.dto.auth.RegisterRequest;
import ru.lakeevda.redisapp.presentation.api.dto.user.UserResponse;
import ru.lakeevda.redisapp.presentation.api.mapper.UserResponseMapper;

/**
 * REST-контроллер аутентификации и регистрации.
 *
 * @author lakeev-da
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserUseCase userUseCase;

    /**
     * Возвращает данные текущего аутентифицированного пользователя.
     *
     * @param authentication данные аутентификации
     * @return данные пользователя или статус 401
     */
    @GetMapping("/login")
    public ResponseEntity<UserResponse> login(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return ResponseEntity.ok(UserResponseMapper.fromDomain(
                    userUseCase.getById(Long.valueOf(authentication.getName()))));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Регистрирует нового пользователя.
     *
     * @param request данные регистрации
     * @return созданный пользователь
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        UserResponse user = UserResponseMapper.fromDomain(userUseCase.create(new CreateUserCommand(
                request.email(),
                request.username(),
                request.password())));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
