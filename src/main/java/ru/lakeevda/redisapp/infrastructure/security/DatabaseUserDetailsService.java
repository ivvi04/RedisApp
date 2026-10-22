package ru.lakeevda.redisapp.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.application.port.out.UserRepository;
import ru.lakeevda.redisapp.domain.enums.UserRole;
import ru.lakeevda.redisapp.domain.model.User;

/**
 * Аутентификация по email. В качестве имени принципала используется id пользователя,
 * чтобы смена email не ломала уже открытую сессию.
 *
 * @author lakeev-da
 */
@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по email и формирует его учётные данные.
     *
     * @param email email пользователя
     * @return данные пользователя для Spring Security
     * @throws UsernameNotFoundException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email не указан");
        }

        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email не найден: " + email));

        UserRole role = user.role() != null ? user.role() : UserRole.USER;

        return org.springframework.security.core.userdetails.User.withUsername(String.valueOf(user.id()))
                .password(user.password())
                .roles(role.name())
                .build();
    }
}
