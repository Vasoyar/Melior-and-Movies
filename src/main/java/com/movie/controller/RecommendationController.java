package com.movie.controller;

import com.movie.model.dto.MovieDTO;
import com.movie.model.dto.SwipeRequest;
import com.movie.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
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
            @RequestHeader("X-User-ID") Long userId,
            @RequestParam(required = false) String context) {

        MovieDTO recommendation = recommendationService.getNextRecommendation(userId, context);

        if (recommendation == null) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "No recommendations available");
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .header("X-Match-Score", String.format("%.2f", recommendation.getMatchScore()))
                .header("X-Recommendation-ID", recommendation.getImdbId())
                .header("Cache-Control", "private, max-age=60")
                .body(recommendation);
    }

    @PostMapping("/swipe")
    public ResponseEntity<?> processSwipe(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody SwipeRequest request) {

        recommendationService.processSwipe(userId, request.getMovieId(), request.getLiked());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "processed");
        response.put("userId", userId);
        response.put("movieId", request.getMovieId());
        response.put("liked", request.getLiked());

        return ResponseEntity.ok()
                .header("X-Profile-Updated", "true")
                .body(response);
    }
}