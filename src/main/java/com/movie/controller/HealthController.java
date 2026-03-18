package com.movie.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public HealthController(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "movie-recommender");
        status.put("timestamp", LocalDateTime.now().toString());

        Map<String, Object> checks = new HashMap<>();

        // Проверка базы данных
        Map<String, Object> dbCheck = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            boolean valid = conn.isValid(1);
            dbCheck.put("status", valid ? "UP" : "DOWN");
            dbCheck.put("database", conn.getMetaData().getDatabaseProductName());

            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            dbCheck.put("query", "OK");

        } catch (Exception e) {
            dbCheck.put("status", "DOWN");
            dbCheck.put("error", e.getMessage());
        }
        checks.put("database", dbCheck);

        status.put("checks", checks);
        status.put("status", "UP".equals(dbCheck.get("status")) ? "UP" : "DOWN");

        if ("UP".equals(status.get("status"))) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }
}