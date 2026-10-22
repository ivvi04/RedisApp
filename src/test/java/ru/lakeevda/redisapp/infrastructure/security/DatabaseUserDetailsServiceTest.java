package ru.lakeevda.redisapp.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Тесты загрузки пользователя для Spring Security.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class DatabaseUserDetailsServiceTest {

    private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DatabaseUserDetailsService service;

    /**
     * Проверяет успешную загрузку пользователя и формирование полномочий.
     */
    @Test
    void loadUserByUsernameReturnsUserDetails() {
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user(UserRole.ADMIN)));

        UserDetails details = service.loadUserByUsername(" user@example.com ");

        assertThat(details.getUsername()).isEqualTo("5");
        assertThat(details.getPassword()).isEqualTo("hash");
        assertThat(details.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }

    /**
     * Проверяет роль по умолчанию, если у пользователя она не задана.
     */
    @Test
    void loadUserByUsernameUsesUserRoleByDefault() {
        when(userRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user(null)));

        UserDetails details = service.loadUserByUsername("user@example.com");

        assertThat(details.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_USER");
    }

    /**
     * Проверяет отказ при пустом email.
     */
    @Test
    void loadUserByUsernameRejectsBlankEmail() {
        assertThatThrownBy(() -> service.loadUserByUsername("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email не указан");

        verifyNoInteractions(userRepository);
    }

    /**
     * Проверяет отказ при отсутствии пользователя.
     */
    @Test
    void loadUserByUsernameThrowsWhenUserNotFound() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@example.com");
    }

    /**
     * Создаёт доменную модель пользователя.
     *
     * @param role роль пользователя
     * @return пользователь
     */
    private User user(UserRole role) {
        return new User(5L, "user@example.com", "Alice", "hash", CREATED_AT, role);
    }
}
