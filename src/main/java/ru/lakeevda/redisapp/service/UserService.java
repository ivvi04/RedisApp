package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.dto.user.UserUpdateRequest;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.mapper.UserMapper;
import ru.lakeevda.redisapp.repository.UserRepository;

import java.time.Instant;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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
        if (userRepository.existsByEmail(request.email())) {
            throw new EntityExistsException("Email already exists");
        }
        UserEntity user = UserMapper.toEntity(request);
        user.setCreatedAt(Instant.now());
        return UserMapper.fromEntity(userRepository.save(user));
    }
    
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        UserEntity existing = getEntityById(id);
        existing.setUsername(request.username());
        existing.setEmail(request.email());
        return UserMapper.fromEntity(userRepository.save(existing));
    }
    
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public UserEntity getEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
}
