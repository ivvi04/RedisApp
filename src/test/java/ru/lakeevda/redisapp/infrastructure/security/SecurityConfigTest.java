package ru.lakeevda.redisapp.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты конфигурации безопасности.
 *
 * @author lakeev-da
 */
class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    /**
     * Проверяет, что используется BCrypt и хеш совпадает с исходным паролем.
     */
    @Test
    void passwordEncoderUsesBcrypt() {
        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        String hash = encoder.encode("secret");
        assertThat(hash).isNotEqualTo("secret");
        assertThat(encoder.matches("secret", hash)).isTrue();
    }
}
