
package com.example.Auth.Service.dto;


import com.example.Auth.Service.entity.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RegisterRequest(

    @NotBlank
    String fullName,

    @NotBlank
    @Email
    String email,

    @NotBlank
    String password,

    @NotNull
    Role role,

    @Positive
    Long studentId

) {}
