package com.example.Auth.Service.security;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.Auth.Service.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;



@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.expiration}") long expiration
    ) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                "JWT secret must contain at least 32 bytes"
            );
        }

        this.secretKey = Keys.hmacShaKeyFor(
            secret.getBytes(StandardCharsets.UTF_8)
        );

        this.expiration = expiration;
    }

    public String generateToken(User user) {

        Date now = new Date();

        return Jwts.builder()
            .subject(user.getEmail())
            .claim("role", user.getRole().name())
            .claim("userId", user.getId())
            .claim("studentId", user.getStudentId())
            .issuedAt(now)
            .expiration(new Date(now.getTime() + expiration))
            .signWith(secretKey)
            .compact();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(
        String token,
        UserDetails userDetails
    ) {
        String email = extractEmail(token);

        return email.equals(userDetails.getUsername())
            && extractAllClaims(token).getExpiration().after(new Date())
            && userDetails.isEnabled();
    }

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
