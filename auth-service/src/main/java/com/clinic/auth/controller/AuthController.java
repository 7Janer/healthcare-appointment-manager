package com.clinic.auth.controller;

import com.clinic.auth.dto.AuthDtos.*;
import com.clinic.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return service.registerPatient(request); }
    @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }
    @PostMapping("/admin/users") @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStaff(@RequestHeader("X-User-Role") String role, @Valid @RequestBody CreateUserRequest request) {
        if (!"ADMIN".equals(role)) throw new org.springframework.security.access.AccessDeniedException("Admin only");
        return service.createStaff(request);
    }
}
