
package com.example.API.Gateway.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import com.example.API.Gateway.service.JwtService;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String path = request.getURI().getPath();
        HttpMethod method = request.getMethod();

        // Allow browser CORS preflight requests.
        if (method == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // Login is public.
        if (path.equals("/api/auth/login")) {
            return chain.filter(exchange);
        }

        // Read Authorization header.
        String authHeader = request.getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        Claims claims;

        try {
            if (!jwtService.isTokenValid(token)) {
                return unauthorized(exchange);
            }

            claims = jwtService.extractClaims(token);

        } catch (Exception e) {
            return unauthorized(exchange);
        }

        String role = claims.get("role", String.class);

        if (role == null ||
                !isAllowed(role, path, method, claims)) {
            return forbidden(exchange);
        }

        // Forward verified identity headers.
        ServerHttpRequest updatedRequest = request.mutate()
                .headers(headers -> {

                    // Remove client-supplied identity headers.
                    headers.remove("X-Auth-Email");
                    headers.remove("X-Auth-Role");
                    headers.remove("X-Auth-User-Id");
                    headers.remove("X-Auth-Student-Id");

                    headers.add(
                            "X-Auth-Email",
                            claims.getSubject()
                    );

                    headers.add(
                            "X-Auth-Role",
                            role
                    );

                    Object userId = claims.get("userId");

                    if (userId != null) {
                        headers.add(
                                "X-Auth-User-Id",
                                String.valueOf(userId)
                        );
                    }

                    Object studentId = claims.get("studentId");

                    if (studentId != null) {
                        headers.add(
                                "X-Auth-Student-Id",
                                String.valueOf(studentId)
                        );
                    }
                })
                .build();

        return chain.filter(
                exchange.mutate()
                        .request(updatedRequest)
                        .build()
        );
    }

    // --------------------------------------------------
    // ROLE-BASED AUTHORIZATION
    // --------------------------------------------------

    private boolean isAllowed(
            String role,
            String path,
            HttpMethod method,
            Claims claims) {

        // ADMIN: Full access.
        if ("ADMIN".equals(role)) {
            return true;
        }

        // FACULTY permissions.
        if ("FACULTY".equals(role)) {

            // Faculty cannot register users.
            if (path.startsWith("/api/auth/register")) {
                return false;
            }

            // Faculty can use AI Assistant.
            if (path.equals("/api/ai/chat")) {
                return method == HttpMethod.POST;
            }

            // Faculty can view students.
            if (path.equals("/api/students")
                    || path.startsWith("/api/students/")) {
                return method == HttpMethod.GET;
            }

            // Faculty can manage courses.
            if (path.equals("/api/courses")
                    || path.startsWith("/api/courses/")) {
                return isReadOrWriteMethod(method);
            }

            // Faculty can manage marks.
            if (path.equals("/api/marks")
                    || path.startsWith("/api/marks/")) {
                return isReadOrWriteMethod(method);
            }

            // Faculty can manage attendance.
            if (path.equals("/api/attendance")
                    || path.startsWith("/api/attendance/")) {
                return isReadOrWriteMethod(method);
            }

            // Faculty can access their own profile.
            return path.equals("/api/auth/me")
                    && method == HttpMethod.GET;
        }

        // STUDENT permissions.
        if ("STUDENT".equals(role)) {

            // Student can view their own account details.
            if (path.equals("/api/auth/me")) {
                return method == HttpMethod.GET;
            }

            // Student can use AI Assistant.
            if (path.equals("/api/ai/chat")) {
                return method == HttpMethod.POST;
            }

            // Students have read-only access to other resources.
            if (method != HttpMethod.GET) {
                return false;
            }

            Object studentIdClaim = claims.get("studentId");

            if (studentIdClaim == null) {
                return false;
            }

            String studentId = String.valueOf(studentIdClaim);

            // Student can view only their own profile.
            if (path.matches("/api/students/\\d+")) {

                String requestedId = path.substring(
                        path.lastIndexOf("/") + 1
                );

                return studentId.equals(requestedId);
            }

            // Student can view their own marks.
            if (path.startsWith("/api/marks/")) {

                return hasMatchingStudentId(
                        path,
                        studentId,
                        "^/api/marks/student/(\\d+)(?:/semester/\\d+)?$"
                );
            }

            // Student can view their own attendance.
            if (path.startsWith("/api/attendance/")) {

                return hasMatchingStudentId(
                        path,
                        studentId,
                        "^/api/attendance/student/(\\d+)(?:/(?:range|percentage))?$"
                );
            }

            // Deny all other student requests.
            return false;
        }

        return false;
    }

    // --------------------------------------------------
    // CHECK STUDENT ID FROM URL PATH
    // --------------------------------------------------

    private boolean hasMatchingStudentId(
            String path,
            String jwtStudentId,
            String regex) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(path);

        if (!matcher.matches()) {
            return false;
        }

        String requestedStudentId = matcher.group(1);

        return jwtStudentId.equals(requestedStudentId);
    }

    // --------------------------------------------------
    // HTTP METHODS
    // --------------------------------------------------

    private boolean isReadOrWriteMethod(HttpMethod method) {

        return method == HttpMethod.GET
                || method == HttpMethod.POST
                || method == HttpMethod.PUT
                || method == HttpMethod.DELETE
                || method == HttpMethod.PATCH;
    }

    // --------------------------------------------------
    // UNAUTHORIZED RESPONSE
    // --------------------------------------------------

    private Mono<Void> unauthorized(ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }

    // --------------------------------------------------
    // FORBIDDEN RESPONSE
    // --------------------------------------------------

    private Mono<Void> forbidden(ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.FORBIDDEN);

        return exchange.getResponse().setComplete();
    }
}
