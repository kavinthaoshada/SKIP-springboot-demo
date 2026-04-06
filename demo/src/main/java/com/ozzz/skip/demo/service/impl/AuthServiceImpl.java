package com.ozzz.skip.demo.service.impl;

import com.ozzz.skip.demo.dto.request.LoginRequest;
import com.ozzz.skip.demo.dto.request.RegisterRequest;
import com.ozzz.skip.demo.dto.response.AuthResponse;
import com.ozzz.skip.demo.exception.BusinessException;
import com.ozzz.skip.demo.model.User;
import com.ozzz.skip.demo.repository.UserRepository;
import com.ozzz.skip.demo.security.JwtUtils;
import com.ozzz.skip.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Check uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email is already registered: " + request.getEmail());
        }

        // Build and save the new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // never store plain text
                .fullName(request.getFullName())
                .role(request.getRole())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .isActive(true)
                .build();

        userRepository.save(user);

        // Generate JWT and return auth response
        String token = jwtUtils.generateTokenFromUsername(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        // Spring Security handles credential validation
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Store authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT
        String token = jwtUtils.generateTokenFromUsername(request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}