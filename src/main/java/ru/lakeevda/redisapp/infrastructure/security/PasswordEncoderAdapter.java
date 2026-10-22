package ru.lakeevda.redisapp.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.lakeevda.redisapp.application.port.out.PasswordEncoder;

/**
 * Адаптер хеширования паролей на основе Spring Security.
 *
 * @author lakeev-da
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoder {

    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * Хеширует пароль.
     *
     * @param rawPassword исходный пароль
     * @return хеш пароля
     */
    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
