package com.movie.controller;

import com.movie.model.Movie;
import com.movie.service.OmdbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final OmdbService omdbService;

    public MovieController(OmdbService omdbService) {
        this.omdbService = omdbService;
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "message", "Movie controller is working!",
                "timestamp", LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/test-omdb")
    public ResponseEntity<?> testOmdb() {
        try {
            boolean isConnected = omdbService.testOmdbConnection();

            if (isConnected) {
                return ResponseEntity.ok(Map.of(
                        "status", "connected",
                        "message", "OMDB API is working"
                ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 503,
                                "error", "Service Unavailable",
                                "message", "OMDB API is not responding",
                                "path", "/api/movies/test-omdb"
                        ));
            }
        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", "Failed to check OMDB connection",
                            "path", "/api/movies/test-omdb"
                    ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query) {
        try {
            System.out.println("Контроллер: поиск фильмов по запросу: " + query);

            List<Movie> movies = omdbService.searchMovies(query);

            if (movies.isEmpty()) {
                return ResponseEntity.ok()
                        .header("X-Total-Count", "0")
                        .body(List.of());  // Пустой список, но 200 OK
            }

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(movies.size()))
                    .body(movies);

        } catch (Exception e) {
            System.err.println("Ошибка в контроллере поиска: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "path", "/api/movies/search"
                    ));
        }
    }

    @GetMapping("/fetch/{imdbId}")
    public ResponseEntity<?> fetchMovie(@PathVariable String imdbId) {
        try {
            System.out.println("Контроллер: получение фильма по ID: " + imdbId);

            Movie movie = omdbService.getMovieById(imdbId);

            if (movie == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "timestamp", LocalDateTime.now().toString(),
                                "status", 404,
                                "error", "Not Found",
                                "message", "Movie not found with IMDb ID: " + imdbId,
                                "path", "/api/movies/fetch/" + imdbId
                        ));
            }

            return ResponseEntity.ok(movie);

        } catch (Exception e) {
            System.err.println("Ошибка в контроллере: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "timestamp", LocalDateTime.now().toString(),
                            "status", 500,
                            "error", "Internal Server Error",
                            "message", e.getMessage(),
                            "path", "/api/movies/fetch/" + imdbId
                    ));
        }
    }
}