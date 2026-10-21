package ru.lakeevda.redisapp.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.UserCreateRequest;
import ru.lakeevda.redisapp.dto.UserResponse;
import ru.lakeevda.redisapp.dto.UserUpdateRequest;
import ru.lakeevda.redisapp.entity.UserEntity;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.mapper.UserMapper;
import ru.lakeevda.redisapp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public Page<UserEntity> getAllUsers(
            @PageableDefault(sort = "createdAt", size = 10) Pageable pageable,
            @RequestParam(defaultValue = "createdAt") String sort) {
        if (pageable == null || sort == null) {
            throw new IllegalArgumentRequestException();
        }
        return userService.getAllUsers(pageable, sort);
    }
    
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        if (userId == null) {
            throw new IllegalArgumentRequestException();
        }
        UserEntity user = userService.getById(userId);
        return UserMapper.toResponse(user);
    }
    
    @PostMapping
    public UserResponse create(@RequestBody UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }
        UserEntity user = UserMapper.toEntity(request);
        user = userService.create(user);
        return UserMapper.toResponse(user);
    }
    
    @PutMapping("/{userId}")
    public UserResponse update(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        if (userId == null || request == null) {
            throw new IllegalArgumentRequestException();
        }
        UserEntity user = UserMapper.toEntity(request);
        user = userService.update(userId, user);
        return UserMapper.toResponse(user);
    }
    
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        if (userId == null) {
            throw new IllegalArgumentRequestException();
        }
        userService.delete(userId);
    }
}
