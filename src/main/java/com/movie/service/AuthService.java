package com.movie.service;

import com.movie.model.dto.AuthRequest;
import com.movie.model.dto.AuthResponse;
import com.movie.model.User;
import com.movie.model.UserPreference;
import com.movie.repository.UserRepository;
import com.movie.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;

    public AuthService(UserRepository userRepository, UserPreferenceRepository preferenceRepository) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public AuthResponse register(AuthRequest request) {
        // Проверка существования
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Создание пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        User savedUser = userRepository.save(user);

        // Создание предпочтений
        UserPreference preferences = new UserPreference(savedUser);
        preferenceRepository.save(preferences);

        // Генерация токена
        String token = "token-" + UUID.randomUUID().toString();

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

        String token = "token-" + UUID.randomUUID().toString();

        AuthResponse response = new AuthResponse();
        response.setMessage("Login successful");
        response.setUserId(user.getId());
        response.setToken(token);
        response.setUsername(user.getUsername());

        return response;
    }
}