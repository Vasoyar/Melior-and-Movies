package com.movie.controller;

import com.movie.dto.MovieDTO;
import com.movie.dto.MovieListItemDTO;
import com.movie.service.OmdbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "OMDB API not responding"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ПОИСК ФИЛЬМОВ С ПАГИНАЦИЕЙ И ЛЁГКИМ DTO
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            // 1. Получаем все результаты (они уже сохранились в БД внутри сервиса)
            List<MovieListItemDTO> allResults = omdbService.searchMovies(query);

            // 2. Пагинация вручную
            int start = page * size;
            int end = Math.min(start + size, allResults.size());

            if (start >= allResults.size()) {
                return ResponseEntity.ok()
                        .header("X-Total-Count", "0")
                        .header("X-Total-Pages", "0")
                        .body(List.of());
            }

            List<MovieListItemDTO> pagedResults = allResults.subList(start, end);

            // 3. Возвращаем с заголовками
            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(allResults.size()))
                    .header("X-Total-Pages", String.valueOf((allResults.size() + size - 1) / size))
                    .header("X-Current-Page", String.valueOf(page))
                    .header("X-Page-Size", String.valueOf(size))
                    .body(pagedResults);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ПОЛУЧЕНИЕ ПОЛНОЙ ИНФОРМАЦИИ О ФИЛЬМЕ
     */
    @GetMapping("/fetch/{imdbId}")
    public ResponseEntity<?> fetchMovie(@PathVariable String imdbId) {
        try {
            MovieDTO movie = omdbService.getFullMovieInfo(imdbId);
            if (movie == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Movie not found"));
            }
            return ResponseEntity.ok(movie);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}