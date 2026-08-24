package com.clinic.auth.dto;

import com.clinic.auth.domain.Role;
import jakarta.validation.constraints.*;
import java.util.UUID;

public final class AuthDtos {
    private AuthDtos() {}
    public record RegisterRequest(
            @NotBlank @Size(max=100) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min=8,max=100) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record CreateUserRequest(
            @NotBlank @Size(max=100) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min=8,max=100) String password,
            @NotNull Role role) {}
    public record AuthResponse(String token, UUID userId, String fullName, String email, Role role) {}
    public record UserResponse(UUID id, String fullName, String email, Role role) {}
}
