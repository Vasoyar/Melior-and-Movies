package com.movie.controller;

import com.movie.model.Collection;
import com.movie.service.CollectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collections")
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping
    public ResponseEntity<?> createCollection(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody Map<String, String> request) {

        String title = request.get("title");
        String description = request.get("description");

        Collection collection = collectionService.createCollection(userId, title, description);

        return ResponseEntity.ok()
                .header("Location", "/api/collections/" + collection.getId())
                .header("X-Collection-ID", collection.getId().toString())
                .body(collection);
    }

    @PostMapping("/{collectionId}/movies/{movieId}")
    public ResponseEntity<?> addMovieToCollection(
            @PathVariable Long collectionId,
            @PathVariable String movieId) {

        try {
            String result = collectionService.addMovieToCollection(collectionId, movieId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", result);
            response.put("collectionId", collectionId);
            response.put("movieId", movieId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(404).body(error);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUserCollections(
            @RequestHeader("X-User-ID") Long userId) {

        List<Collection> collections = collectionService.getUserCollections(userId);
        return ResponseEntity.ok(collections);
    }
}