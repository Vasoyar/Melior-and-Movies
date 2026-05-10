package com.movie;

import com.movie.dto.AuthRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRequest() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user123");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankUsername_shouldFail() {
        AuthRequest request = new AuthRequest();
        request.setUsername("");
        request.setEmail("user@example.com");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        String actualMessage = violations.iterator().next().getMessage();
        assertThat(actualMessage).matches(msg ->
                msg.equals("Username is required") || msg.equals("Username must be 3-50 characters"));
    }

    @Test
    void invalidEmail_shouldFail() {
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setEmail("not-an-email");
        request.setPassword("password123");

        Set<ConstraintViolation<AuthRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("Invalid email");
    }
}
