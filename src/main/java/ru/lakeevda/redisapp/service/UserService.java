package ru.lakeevda.redisapp.service;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.repository.UserRepository;

import java.time.Instant;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional(readOnly = true)
    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
    
    @Transactional(readOnly = true)
    public Page<UserEntity> getAllUsers(Pageable pageable, String sort) {
        String sortBy = sort != null && !sort.isEmpty() ? sort : "createdAt";
        return userRepository.findAll(pageable);
    }
    
    @Transactional
    public UserEntity create(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityExistsException("Email already exists");
        }
        user.setCreatedAt(Instant.now());
        return userRepository.save(user);
    }
    
    @Transactional
    public UserEntity update(Long id, UserEntity user) {
        UserEntity existing = userRepository.findById(id).orElseThrow();
        existing.setUsername(user.getUsername());
        existing.setEmail(user.getEmail());
        return userRepository.save(existing);
    }
    
    @Transactional
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
