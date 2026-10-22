package ru.lakeevda.redisapp.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Тесты проверки роли администратора.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class SecurityUtilsTest {

    @Mock
    private Authentication authentication;

    /**
     * Проверяет отсутствие аутентификации.
     */
    @Test
    void isAdminReturnsFalseForNullAuthentication() {
        assertThat(SecurityUtils.isAdmin(null)).isFalse();
    }

    /**
     * Проверяет пользователя с ролью администратора.
     */
    @Test
    void isAdminReturnsTrueForAdminAuthority() {
        doReturn(List.of(new SimpleGrantedAuthority(SecurityUtils.ROLE_ADMIN)))
                .when(authentication).getAuthorities();

        assertThat(SecurityUtils.isAdmin(authentication)).isTrue();
    }

    /**
     * Проверяет пользователя без роли администратора.
     */
    @Test
    void isAdminReturnsFalseForUserAuthority() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();

        assertThat(SecurityUtils.isAdmin(authentication)).isFalse();
    }

    /**
     * Проверяет поиск роли среди нескольких полномочий.
     */
    @Test
    void isAdminChecksAllAuthorities() {
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority(SecurityUtils.ROLE_ADMIN));
        doReturn(authorities).when(authentication).getAuthorities();

        assertThat(SecurityUtils.isAdmin(authentication)).isTrue();
    }

    /**
     * Проверяет пустой список полномочий.
     */
    @Test
    void isAdminReturnsFalseForEmptyAuthorities() {
        doReturn(List.of()).when(authentication).getAuthorities();

        assertThat(SecurityUtils.isAdmin(authentication)).isFalse();
    }
}
