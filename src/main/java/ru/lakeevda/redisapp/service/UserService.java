package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.dto.user.UserUpdateRequest;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.mapper.UserMapper;
import ru.lakeevda.redisapp.repository.UserRepository;

import java.time.Instant;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return UserMapper.fromEntity(getEntityById(id));
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserMapper::fromEntity);
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EntityExistsException("Пользователь с таким email уже существует");
        }

        UserEntity user = UserMapper.toEntity(request);
        user.setEmail(email);
        user.setUsername(request.username().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(Instant.now());

        return UserMapper.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity existing = getEntityById(id);
        String email = normalizeEmail(request.email());

        if (!existing.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new EntityExistsException("Пользователь с таким email уже существует");
        }

        existing.setUsername(request.username().trim());
        existing.setEmail(email);

        return UserMapper.fromEntity(userRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public UserEntity getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
