package ru.lakeevda.redisapp.presentation.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.lakeevda.redisapp.application.port.in.UserUseCase;
import ru.lakeevda.redisapp.infrastructure.security.SecurityUtils;
import ru.lakeevda.redisapp.presentation.api.mapper.UserResponseMapper;

/**
 * Общие web-атрибуты для страниц.
 *
 * @author lakeev-da
 */
@ControllerAdvice(assignableTypes = WebController.class)
@RequiredArgsConstructor
public class WebModelAttributes {

    private final UserUseCase userUseCase;

    /**
     * Добавляет в модель данные текущего пользователя и признак администратора.
     *
     * @param model          модель представления
     * @param authentication данные аутентификации
     */
    @ModelAttribute
    public void addCurrentUser(Model model, Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("isAdmin", false);
            return;
        }

        model.addAttribute("isAdmin", SecurityUtils.isAdmin(authentication));

        try {
            model.addAttribute("currentUser", UserResponseMapper.fromDomain(
                    userUseCase.getById(Long.valueOf(authentication.getName()))));
        } catch (RuntimeException ignored) {
            // Пользователь мог быть удалён — страница всё равно должна открыться.
        }
    }
}
