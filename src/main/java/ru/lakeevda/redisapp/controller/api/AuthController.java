package ru.lakeevda.redisapp.controller.api;

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
import ru.lakeevda.redisapp.dto.auth.RegisterRequest;
import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public ResponseEntity<UserResponse> login(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            return ResponseEntity.ok(userService.getById(Long.valueOf(authentication.getName())));
        } catch (RuntimeException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        UserResponse user = userService.create(new UserCreateRequest(
                request.email(),
                request.username(),
                request.password()));

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
