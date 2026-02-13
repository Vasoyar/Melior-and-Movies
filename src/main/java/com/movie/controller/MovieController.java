package com.movie.controller;

import com.movie.service.OmdbService;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final OmdbService omdbService;

    public MovieController(OmdbService omdbService) {
        this.omdbService = omdbService;
    }

    @GetMapping("/test")
    public Map<String, String> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Movie controller is working!");
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return response;
    }



    @GetMapping("/test-omdb")
    public Map<String, Object> testOmdb() {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isConnected = omdbService.testOmdbConnection();

            response.put("success", isConnected);
            response.put("message", isConnected ?
                    "OMDB API работает!" : "OMDB API не отвечает");

            response.put("apiKey", " Ключ настроен в application.properties");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Ошибка: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/search")
    public Map<String, Object> searchMovies(@RequestParam String query) {
        Map<String, Object> response = new HashMap<>();

        try {
            var movies = omdbService.searchMovies(query);
            response.put("success", true);
            response.put("count", movies.size());
            response.put("movies", movies);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/fetch/{imdbId}")
    public Map<String, Object> fetchMovie(@PathVariable String imdbId) {
        Map<String, Object> response = new HashMap<>();

        try {
            var movie = omdbService.getMovieById(imdbId);
            if (movie != null) {
                response.put("success", true);
                response.put("movie", movie);
            } else {
                response.put("success", false);
                response.put("message", "Фильм не найден");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }
}