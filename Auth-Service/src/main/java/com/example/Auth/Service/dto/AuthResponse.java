
package com.example.Auth.Service.dto;

import com.example.Auth.Service.entity.Role;

public record AuthResponse(

    String message,
    String token,
    Long userId,
    String fullName,
    String email,
    Role role,
    Long studentId

) {}
