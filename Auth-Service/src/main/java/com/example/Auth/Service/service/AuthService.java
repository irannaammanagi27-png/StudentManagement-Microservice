package com.example.Auth.Service.service;




import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Auth.Service.dto.AuthResponse;
import com.example.Auth.Service.dto.LoginRequest;
import com.example.Auth.Service.dto.RegisterRequest;
import com.example.Auth.Service.entity.Role;
import com.example.Auth.Service.entity.User;
import com.example.Auth.Service.repository.UserRepository;
import com.example.Auth.Service.security.JwtService;



@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.email(),
                    request.password()
                )
            );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateToken(user);

        return new AuthResponse(
            "Login successful",
            token,
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getStudentId()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                "Email already registered"
            );
        }

        if (request.role() == Role.STUDENT && request.studentId() == null) {
            throw new IllegalArgumentException(
                "Student ID is required for Student registration"
            );
        }

        if (request.role() != Role.STUDENT && request.studentId() != null) {
            throw new IllegalArgumentException(
                "Student ID is only applicable to Student accounts"
            );
        }

        User user = new User();

        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStudentId(request.studentId());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        return new AuthResponse(
            "User registered successfully",
            null,
            savedUser.getId(),
            savedUser.getFullName(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getStudentId()
        );
    }

    public AuthResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );

        return new AuthResponse(
            "User details fetched successfully",
            null,
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getStudentId()
        );
    }
}

