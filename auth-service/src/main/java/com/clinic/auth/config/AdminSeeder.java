package com.clinic.auth.config;

import com.clinic.auth.domain.Role;
import com.clinic.auth.domain.UserAccount;
import com.clinic.auth.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {
    @Bean CommandLineRunner seedAdmin(UserAccountRepository users, PasswordEncoder encoder,
            @Value("${app.admin.email}") String email, @Value("${app.admin.password}") String password) {
        return args -> {
            if (!users.existsByEmailIgnoreCase(email)) {
                users.save(new UserAccount("Clinic Administrator", email, encoder.encode(password), Role.ADMIN));
            }
        };
    }
}
