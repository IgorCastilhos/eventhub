package com.eventhub.service;

import com.eventhub.dto.request.LoginRequest;
import com.eventhub.dto.request.RegisterRequest;
import com.eventhub.dto.response.AuthResponse;
import com.eventhub.entity.User;
import com.eventhub.enums.Role;
import com.eventhub.exception.BusinessException;
import com.eventhub.repository.UserRepository;
import com.eventhub.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Attempting to register user: {}", request.username());

        String normalizedUsername = request.getNormalizedUsername();
        String normalizedEmail = request.getNormalizedEmail();
        if (userRepository.existsByUsername(normalizedUsername)) {
            log.warn("Registration failed: Username already exists: {}", normalizedUsername);
            throw new BusinessException("Username is already taken");
        }
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Registration failed: Email already exists: {}", normalizedEmail);
            throw new BusinessException("Email is already registered");
        }
        String passwordHash = passwordEncoder.encode(request.password());
        User user = User.builder()
                .username(request.username())
                .email(normalizedEmail)
                .passwordHash(passwordHash)
                .role(Role.USER)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(savedUser);

        // TODO: Send welcome email (async)
        // emailService.sendWelcomeEmail(savedUser);

        // Return response
        return new AuthResponse(
                token,
                savedUser.getUsername(),
                savedUser.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.username());
        User user = userRepository.findByUsernameOrEmail(
                        request.username(),
                        request.username()
                )
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found: {}", request.username());
                    return new BadCredentialsException("Invalid credentials");
                });
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: Invalid password for user: {}", user.getUsername());
            throw new BadCredentialsException("Invalid credentials");
        }
        if (!user.isEnabled()) {
            log.warn("Login failed: Account disabled: {}", user.getUsername());
            throw new BusinessException("Account is disabled. Please contact support.");
        }
        if (!user.isAccountNonLocked()) {
            log.warn("Login failed: Account locked: {}", user.getUsername());
            throw new BusinessException("Account is locked. Please contact support.");
        }
        String token = jwtTokenProvider.generateToken(user);
        log.info("User logged in successfully: {}", user.getUsername());
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getRole()
        );
    }

    @Transactional(readOnly = true)
    public User validateTokenAndGetUser(String token) {
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }
        String username = jwtTokenProvider.getUsernameFromToken(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));
    }
}