package ru.lakeevda.redisapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(max = 255) String username) {
}
