package ru.lakeevda.redisapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Заставляет Spring Security записать CSRF-cookie при первом рендере страницы,
 * чтобы JavaScript мог отправлять заголовок X-XSRF-TOKEN.
 *
 * @author lakeev-da
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    /**
     * Инициализирует CSRF-токен и передаёт запрос дальше по цепочке фильтров.
     *
     * @param request     HTTP-запрос
     * @param response    HTTP-ответ
     * @param filterChain цепочка фильтров
     * @throws ServletException при ошибке обработки сервлета
     * @throws IOException      при ошибке ввода-вывода
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
