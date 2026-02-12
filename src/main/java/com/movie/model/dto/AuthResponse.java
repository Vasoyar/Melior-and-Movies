package com.movie.model.dto;

public class AuthResponse {

    private String message;
    private Long userId;
    private String token;
    private String username;

    // Конструкторы
    public AuthResponse() {}

    public AuthResponse(String message, Long userId, String token, String username) {
        this.message = message;
        this.userId = userId;
        this.token = token;
        this.username = username;
    }

    // Геттеры и Сеттеры
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}