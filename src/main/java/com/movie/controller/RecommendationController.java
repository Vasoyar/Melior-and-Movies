package com.movie.controller;

import com.movie.dto.MovieDTO;
import com.movie.dto.SwipeRequest;
import com.movie.service.RecommendationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/next")
    public ResponseEntity<?> getNextRecommendation(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestParam(required = false) String context) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/recommendations/next"
                    ));
        }

        try {
            MovieDTO recommendation = recommendationService.getNextRecommendation(userId, context);

            if (recommendation == null) {
                return ResponseEntity
                        .noContent()
                        .header("X-Total-Available", "0")
                        .build();
            }

            return ResponseEntity.ok()
                    .header("X-Match-Score", String.format("%.2f", recommendation.getMatchScore()))
                    .header("X-Recommendation-ID", recommendation.getImdbId())
                    .body(recommendation);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("User not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 404,
                                "error", "Not Found",
                                "message", "User not found with id: " + userId,
                                "path", "/api/recommendations/next"
                        ));
            }

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "path", "/api/recommendations/next"
                    ));
        }
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> processSwipe(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestBody SwipeRequest request) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/recommendations/swipe"
                    ));
        }

        if (request.getMovieId() == null || request.getLiked() == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "movieId and liked are required",
                            "path", "/api/recommendations/swipe"
                    ));
        }

        try {
            recommendationService.processSwipe(userId, request.getMovieId(), request.getLiked());

            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .header("X-Profile-Updated", "true")
                    .build();

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/api/recommendations/swipe"
                    ));
        }
    }
}