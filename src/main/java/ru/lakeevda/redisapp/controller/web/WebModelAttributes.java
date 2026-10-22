package ru.lakeevda.redisapp.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.security.SecurityUtils;
import ru.lakeevda.redisapp.service.UserService;

/**
 * Общие web-атрибуты для страниц.
 *
 * @author lakeev-da
 */
@ControllerAdvice(assignableTypes = WebController.class)
@RequiredArgsConstructor
public class WebModelAttributes {

    private final UserService userService;

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
            UserResponse user = userService.getById(Long.valueOf(authentication.getName()));
            model.addAttribute("currentUser", user);
        } catch (RuntimeException ignored) {
            // Пользователь мог быть удалён — страница всё равно должна открыться.
        }
    }
}
