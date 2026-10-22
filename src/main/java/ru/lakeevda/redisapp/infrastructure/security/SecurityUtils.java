package ru.lakeevda.redisapp.infrastructure.security;

import org.springframework.security.core.Authentication;

/**
 * Вспомогательные методы для работы с ролями Spring Security.
 *
 * @author lakeev-da
 */
public final class SecurityUtils {

    /**
     * Имя роли администратора.
     */
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Утилитарный класс не предназначен для создания экземпляров.
     */
    private SecurityUtils() {
    }

    /**
     * Проверяет, обладает ли текущий пользователь ролью администратора.
     *
     * @param authentication данные аутентификации
     * @return {@code true}, если пользователь администратор
     */
    public static boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> ROLE_ADMIN.equals(authority.getAuthority()));
    }
}
