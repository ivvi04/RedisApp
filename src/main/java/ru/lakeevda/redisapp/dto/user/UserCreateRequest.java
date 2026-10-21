package ru.lakeevda.redisapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 255) String username,
        @NotBlank @Size(max = 255) String password) {
}
