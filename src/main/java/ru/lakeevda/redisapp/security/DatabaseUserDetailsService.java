package ru.lakeevda.redisapp.security;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.enums.UserRole;
import ru.lakeevda.redisapp.repository.UserRepository;

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

        UserEntity user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email не найден: " + email));

        UserRole role = user.getRole() != null ? UserRole.fromValue(user.getRole()) : UserRole.USER;

        return User.withUsername(String.valueOf(user.getId()))
                .password(user.getPassword())
                .roles(role.name())
                .build();
    }
}
