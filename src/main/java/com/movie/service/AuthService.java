package com.movie.service;

import com.movie.config.JwtTokenUtil;               // ← импорт JwtTokenUtil
import com.movie.dto.AuthRequest;
import com.movie.dto.AuthResponse;
import com.movie.model.User;
import com.movie.model.UserPreference;
import com.movie.repository.UserRepository;
import com.movie.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final JwtTokenUtil jwtTokenUtil;        // ← 1. ДОБАВЛЕНО поле

    // Конструктор: добавляем JwtTokenUtil как зависимость
    public AuthService(UserRepository userRepository,
                       UserPreferenceRepository preferenceRepository,
                       JwtTokenUtil jwtTokenUtil) { // ← 2. ДОБАВЛЕН параметр
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.jwtTokenUtil = jwtTokenUtil;          // ← 3. ИНИЦИАЛИЗАЦИЯ
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // в реальном проекте нужно хешировать!

        User savedUser = userRepository.save(user);

        UserPreference preferences = new UserPreference(savedUser);
        preferenceRepository.save(preferences);

        // РЕАЛЬНЫЙ JWT-ТОКЕН вместо заглушки
        String token = jwtTokenUtil.generateToken(savedUser.getUsername());

        AuthResponse response = new AuthResponse();
        response.setMessage("User registered successfully");
        response.setUserId(savedUser.getId());
        response.setToken(token);
        response.setUsername(savedUser.getUsername());

        return response;
    }

    public AuthResponse login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // РЕАЛЬНЫЙ JWT-ТОКЕН
        String token = jwtTokenUtil.generateToken(user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful");
        response.setUserId(user.getId());
        response.setToken(token);
        response.setUsername(user.getUsername());

        return response;
    }
}