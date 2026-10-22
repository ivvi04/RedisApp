package ru.lakeevda.redisapp.controller.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.lakeevda.redisapp.security.SecurityUtils;

/**
 * Web-контроллер страниц аутентификации.
 * Сама аутентификация выполняется фильтрами Spring Security, а не контроллером.
 */
@Controller
public class WebAuthController {

    @GetMapping("/login")
    public String login(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:" + homePage(authentication) : "web/login";
    }

    @GetMapping("/register")
    public String register(Authentication authentication) {
        return isAuthenticated(authentication) ? "redirect:" + homePage(authentication) : "web/register";
    }

    private String homePage(Authentication authentication) {
        return SecurityUtils.isAdmin(authentication) ? "/" : "/chats";
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
