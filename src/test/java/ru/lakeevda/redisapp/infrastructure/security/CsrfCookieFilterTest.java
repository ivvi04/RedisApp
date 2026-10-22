package ru.lakeevda.redisapp.infrastructure.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Тесты фильтра инициализации CSRF-токена.
 *
 * @author lakeev-da
 */
class CsrfCookieFilterTest {

    private final CsrfCookieFilter filter = new CsrfCookieFilter();

    /**
     * Проверяет чтение токена и продолжение цепочки фильтров.
     *
     * @throws Exception при ошибке выполнения фильтра
     */
    @Test
    void doFilterInternalInitializesTokenAndContinuesChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        CsrfToken token = mock(CsrfToken.class);
        request.setAttribute(CsrfToken.class.getName(), token);

        filter.doFilter(request, response, chain);

        verify(token).getToken();
        verify(chain).doFilter(request, response);
    }

    /**
     * Проверяет продолжение цепочки фильтров при отсутствии токена.
     *
     * @throws Exception при ошибке выполнения фильтра
     */
    @Test
    void doFilterInternalContinuesChainWithoutToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
