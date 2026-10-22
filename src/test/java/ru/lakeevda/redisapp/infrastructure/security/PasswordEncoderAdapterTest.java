package ru.lakeevda.redisapp.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тесты адаптера кодирования паролей.
 *
 * @author lakeev-da
 */
@ExtendWith(MockitoExtension.class)
class PasswordEncoderAdapterTest {

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Проверяет делегирование кодирования пароля.
     */
    @Test
    void encodeDelegatesToSpringEncoder() {
        when(passwordEncoder.encode("secret")).thenReturn("hash");
        PasswordEncoderAdapter adapter = new PasswordEncoderAdapter(passwordEncoder);

        String result = adapter.encode("secret");

        assertThat(result).isEqualTo("hash");
        verify(passwordEncoder).encode("secret");
    }
}
