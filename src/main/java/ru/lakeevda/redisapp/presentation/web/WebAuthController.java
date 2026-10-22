package ru.lakeevda.redisapp.presentation.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.lakeevda.redisapp.infrastructure.security.SecurityUtils;

/**
 * Web-контроллер страниц аутентификации.
 * Сама аутентификация выполняется фильтрами Spring Security, а не контроллером.
 *
 * @author lakeev-da
 */
@Controller
public class WebAuthController {

    /**
     * Показывает страницу входа или перенаправляет аутентифицированного пользователя.
     *
     * @param authentication данные аутентификации
     * @return имя представления или redirect-URL
     */
    @GetMapping("/login")
    public String login(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:" + homePage(authentication) : "web/login";
    }

    /**
     * Показывает страницу регистрации или перенаправляет аутентифицированного пользователя.
     *
     * @param authentication данные аутентификации
     * @return имя представления или redirect-URL
     */
    @GetMapping("/register")
    public String register(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:" + homePage(authentication) : "web/register";
    }

    /**
     * Определяет домашнюю страницу пользователя в зависимости от роли.
     *
     * @param authentication данные аутентификации
     * @return URL домашней страницы
     */
    private String homePage(Authentication authentication) {
        return SecurityUtils.isAdmin(authentication) ? "/" : "/chats";
    }

    /**
     * Проверяет, что пользователь аутентифицирован и не является анонимным.
     *
     * @param authentication данные аутентификации
     * @return {@code true}, если пользователь аутентифицирован
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
