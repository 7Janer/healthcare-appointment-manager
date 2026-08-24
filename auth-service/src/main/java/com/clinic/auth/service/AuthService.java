package com.clinic.auth.service;

import com.clinic.auth.domain.Role;
import com.clinic.auth.domain.UserAccount;
import com.clinic.auth.dto.AuthDtos.*;
import com.clinic.auth.repository.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    public AuthService(UserAccountRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }
    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {
        UserAccount user = create(request.fullName(), request.email(), request.password(), Role.PATIENT);
        return response(user);
    }
    @Transactional
    public UserResponse createStaff(CreateUserRequest request) {
        if (request.role() == Role.PATIENT) throw new IllegalArgumentException("Staff role must be DOCTOR or ADMIN");
        UserAccount user = create(request.fullName(), request.email(), request.password(), request.role());
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
    public AuthResponse login(LoginRequest request) {
        UserAccount user = users.findByEmailIgnoreCase(request.email())
                .filter(UserAccount::isActive).orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!encoder.matches(request.password(), user.getPasswordHash())) throw new BadCredentialsException("Invalid credentials");
        return response(user);
    }
    private UserAccount create(String name, String email, String password, Role role) {
        if (users.existsByEmailIgnoreCase(email)) throw new IllegalArgumentException("Email is already registered");
        return users.save(new UserAccount(name.trim(), email.trim(), encoder.encode(password), role));
    }
    private AuthResponse response(UserAccount user) {
        return new AuthResponse(jwt.issue(user), user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
