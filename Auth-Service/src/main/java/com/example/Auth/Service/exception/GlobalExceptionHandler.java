
package com.example.Auth.Service.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(
        BadCredentialsException ex
    ) {
        return buildResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabled(DisabledException ex) {
        return buildResponse(
            HttpStatus.FORBIDDEN,
            "Your account is disabled"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(
        IllegalArgumentException ex
    ) {
        return buildResponse(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        String message = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Invalid request");

        return buildResponse(
            HttpStatus.BAD_REQUEST,
            message
        );
    }

    private ResponseEntity<?> buildResponse(
        HttpStatus status,
        String message
    ) {
        return ResponseEntity.status(status).body(
            Map.of(
                "statusCode", status.value(),
                "message", message,
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }
}
