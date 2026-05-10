package com.movie;

import com.movie.dto.AuthRequest;
import com.movie.dto.AuthResponse;
import com.movie.repository.UserRepository;
import com.movie.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerUser_savesInDatabase() {
        AuthRequest request = new AuthRequest();
        request.setUsername("integuser");
        request.setEmail("integ@test.com");
        request.setPassword("pass123");

        AuthResponse response = authService.register(request);

        assertThat(response.getUserId()).isNotNull();
        assertThat(userRepository.findByUsername("integuser")).isPresent();
        assertThat(userRepository.findByEmail("integ@test.com")).isPresent();
    }
}