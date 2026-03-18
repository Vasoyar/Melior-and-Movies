package com.movie.controller;

import com.movie.model.Collection;
import com.movie.service.CollectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
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
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestBody Map<String, String> request) {


        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections"
                    ));
        }

        // Получаем данные из запроса
        String title = request.get("title");
        String description = request.get("description");

        // Проверяем, что название не пустое
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "Title is required",
                            "path", "/api/collections"
                    ));
        }

        try {

            Collection collection = collectionService.createCollection(userId, title, description);

            URI location = URI.create("/api/collections/" + collection.getId());

            return ResponseEntity
                    .created(location)
                    .header("X-Collection-ID", collection.getId().toString())
                    .body(collection);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 404,
                                "error", "Not Found",
                                "message", "User not found",
                                "path", "/api/collections"
                        ));
            }

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "An unexpected error occurred",
                            "path", "/api/collections"
                    ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserCollections(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections"
                    ));
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Collection> collections = collectionService.getUserCollections(userId, pageable);

            return ResponseEntity
                    .ok()
                    .header("X-Total-Count", String.valueOf(collections.getTotalElements()))
                    .header("X-Total-Pages", String.valueOf(collections.getTotalPages()))
                    .body(collections.getContent());

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 404,
                            "error", "Not Found",
                            "message", "User not found",
                            "path", "/api/collections"
                    ));
        }
    }

    @PostMapping("/{collectionId}/movies/{movieId}")
    public ResponseEntity<?> addMovieToCollection(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @PathVariable Long collectionId,
            @PathVariable String movieId) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections/" + collectionId + "/movies/" + movieId
                    ));
        }

        try {
            String result = collectionService.addMovieToCollection(collectionId, movieId, userId);

            return ResponseEntity
                    .ok()
                    .body(Map.of(
                            "message", result,
                            "collectionId", collectionId,
                            "movieId", movieId
                    ));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 404,
                                "error", "Not Found",
                                "message", e.getMessage(),
                                "path", "/api/collections/" + collectionId + "/movies/" + movieId
                        ));
            }
            if (e.getMessage().contains("don't own")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 403,
                                "error", "Forbidden",
                                "message", e.getMessage(),
                                "path", "/api/collections/" + collectionId + "/movies/" + movieId
                        ));
            }

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "path", "/api/collections/" + collectionId + "/movies/" + movieId
                    ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @PathVariable Long id) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections/" + id
                    ));
        }

        try {
            boolean deleted = collectionService.deleteCollection(id, userId);

            if (deleted) {
                // 204 No Content - успешное удаление
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 404,
                                "error", "Not Found",
                                "message", "Collection not found",
                                "path", "/api/collections/" + id
                        ));
            }

        } catch (RuntimeException e) {
            if (e.getMessage().contains("not own")) {
                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 403,
                                "error", "Forbidden",
                                "message", e.getMessage(),
                                "path", "/api/collections/" + id
                        ));
            }

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "path", "/api/collections/" + id
                    ));
        }
    }


    @PostMapping("/{collectionId}/like")
    public ResponseEntity<?> likeCollection(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @PathVariable Long collectionId) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections/" + collectionId + "/like"
                    ));
        }

        try {
            String result = collectionService.likeCollection(userId, collectionId);

            return ResponseEntity
                    .ok()
                    .body(Map.of(
                            "message", result,
                            "collectionId", collectionId,
                            "likesCount", collectionService.getCollectionLikesCount(collectionId)
                    ));

        } catch (RuntimeException e) {
            if (e.getMessage().contains("already liked")) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 409,
                                "error", "Conflict",
                                "message", e.getMessage(),
                                "path", "/api/collections/" + collectionId + "/like"
                        ));
            }

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/api/collections/" + collectionId + "/like"
                    ));
        }
    }


    @DeleteMapping("/{collectionId}/like")
    public ResponseEntity<?> unlikeCollection(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @PathVariable Long collectionId) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections/" + collectionId + "/like"
                    ));
        }

        try {
            String result = collectionService.unlikeCollection(userId, collectionId);

            return ResponseEntity
                    .ok()
                    .body(Map.of(
                            "message", result,
                            "collectionId", collectionId,
                            "likesCount", collectionService.getCollectionLikesCount(collectionId)
                    ));

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/api/collections/" + collectionId + "/like"
                    ));
        }
    }

    @GetMapping("/{collectionId}/likes/count")
    public ResponseEntity<?> getCollectionLikesCount(@PathVariable Long collectionId) {
        long count = collectionService.getCollectionLikesCount(collectionId);

        return ResponseEntity
                .ok()
                .body(Map.of(
                        "collectionId", collectionId,
                        "likesCount", count
                ));
    }


    @GetMapping("/liked")
    public ResponseEntity<?> getLikedCollections(
            @RequestHeader(value = "X-User-ID", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (userId == null) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 400,
                            "error", "Bad Request",
                            "message", "X-User-ID header is required",
                            "path", "/api/collections/liked"
                    ));
        }

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Collection> likedCollections = collectionService.getLikedCollections(userId, pageable);

            return ResponseEntity
                    .ok()
                    .header("X-Total-Count", String.valueOf(likedCollections.getTotalElements()))
                    .header("X-Total-Pages", String.valueOf(likedCollections.getTotalPages()))
                    .body(likedCollections.getContent());

        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 404,
                            "error", "Not Found",
                            "message", e.getMessage(),
                            "path", "/api/collections/liked"
                    ));
        }
    }
}