package ru.lakeevda.redisapp.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.lakeevda.redisapp.dto.user.UserCreateRequest;
import ru.lakeevda.redisapp.dto.user.UserResponse;
import ru.lakeevda.redisapp.dto.user.UserUpdateRequest;
import ru.lakeevda.redisapp.exception.IllegalArgumentRequestException;
import ru.lakeevda.redisapp.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @GetMapping
    public Page<UserResponse> getAllUsers(
            @PageableDefault(sort = "createdAt") Pageable pageable) {
        if (pageable == null) {
            throw new IllegalArgumentRequestException();
        }
        return userService.getAllUsers(pageable);
    }
    
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable Long userId) {
        if (userId == null) {
            throw new IllegalArgumentRequestException();
        }
        return userService.getById(userId);
    }
    
    @PostMapping
    public UserResponse create(@RequestBody UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentRequestException();
        }
        return userService.create(request);
    }
    
    @PutMapping("/{userId}")
    public UserResponse update(@PathVariable Long userId, @RequestBody UserUpdateRequest request) {
        if (userId == null || request == null) {
            throw new IllegalArgumentRequestException();
        }
        return userService.update(userId, request);
    }
    
    @DeleteMapping("/{userId}")
    public void delete(@PathVariable Long userId) {
        if (userId == null) {
            throw new IllegalArgumentRequestException();
        }
        userService.delete(userId);
    }
}
