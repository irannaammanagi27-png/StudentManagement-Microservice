package com.example.Auth.Service.controller;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.Auth.Service.dto.AuthResponse;
import com.example.Auth.Service.dto.LoginRequest;
import com.example.Auth.Service.dto.RegisterRequest;
import com.example.Auth.Service.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(
        Authentication authentication
    ) {
        return ResponseEntity.ok(
            authService.getCurrentUser(authentication.getName())
        );
    }
}
