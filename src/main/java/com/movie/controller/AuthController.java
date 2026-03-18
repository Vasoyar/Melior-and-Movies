package com.movie.controller;

import com.movie.dto.AuthRequest;
import com.movie.dto.AuthResponse;
import com.movie.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.register(request);

            return ResponseEntity
                    .created(URI.create("/api/users/" + response.getUserId()))
                    .header("X-User-ID", response.getUserId().toString())
                    .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", e.getMessage(),
                            "path", "/api/auth/register"
                    ));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("already exists")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 409,
                                "error", "Conflict",
                                "message", e.getMessage(),
                                "path", "/api/auth/register"
                        ));
            }
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "An unexpected error occurred",
                            "path", "/api/auth/register"
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String username = credentials.get("username");
            String password = credentials.get("password");

            if (username == null || password == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 400,
                                "error", "Bad Request",
                                "message", "Username and password are required",
                                "path", "/api/auth/login"
                        ));
            }

            AuthResponse response = authService.login(username, password);

            return ResponseEntity
                    .ok()
                    .header("X-User-ID", response.getUserId().toString())
                    .header("X-Auth-Status", "authenticated")
                    .body(response);

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 401,
                            "error", "Unauthorized",
                            "message", "Invalid username or password",
                            "path", "/api/auth/login"
                    ));
        }
    }
}