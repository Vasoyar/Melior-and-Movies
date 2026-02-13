package com.movie.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.model.dto.MovieDTO;
import com.movie.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OmdbService {

    @Value("${omdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OmdbService() {
        this.webClient = WebClient.create("http://www.omdbapi.com");
        this.objectMapper = new ObjectMapper();
    }

    public Movie getMovieById(String imdbId) {
        try {
            System.out.println(" Запрос к OMDB API: фильм " + imdbId);

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("i", imdbId)
                            .queryParam("apikey", apiKey)
                            .queryParam("plot", "short")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);

            if (root.has("Response") && "False".equals(root.get("Response").asText())) {
                System.out.println(" OMDB Error: " + root.get("Error").asText());
                return null;
            }

            Movie movie = new Movie();
            movie.setImdbId(root.get("imdbID").asText());
            movie.setTitle(root.get("Title").asText());
            movie.setYear(root.get("Year").asText());
            movie.setGenre(root.get("Genre").asText());
            movie.setPlot(root.get("Plot").asText());
            movie.setPoster(root.get("Poster").asText());
            movie.setImdbRating(root.get("imdbRating").asText());
            movie.setDirector(root.get("Director").asText());
            movie.setRuntime(root.get("Runtime").asText());

            System.out.println("Найден фильм: " + movie.getTitle());
            return movie;

        } catch (Exception e) {
            System.err.println("Ошибка при запросе к OMDB: " + e.getMessage());
            return null;
        }
    }


    public List<Movie> searchMovies(String query) {
        try {
            System.out.println("Поиск в OMDB: " + query);

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("s", query)
                            .queryParam("apikey", apiKey)
                            .queryParam("type", "movie")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            List<Movie> movies = new ArrayList<>();

            if (root.has("Search")) {
                for (JsonNode node : root.get("Search")) {
                    String imdbId = node.get("imdbID").asText();
                    Movie movie = getMovieById(imdbId);
                    if (movie != null) {
                        movies.add(movie);
                    }
                }
                System.out.println("Найдено фильмов: " + movies.size());
            }

            return movies;

        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public boolean testOmdbConnection() {
        try {
            Movie movie = getMovieById("tt0133093"); // The Matrix
            return movie != null;
        } catch (Exception e) {
            return false;
        }
    }


    public MovieDTO convertToDTO(Movie movie, Double matchScore, String explanation) {
        MovieDTO dto = new MovieDTO();
        dto.setImdbId(movie.getImdbId());
        dto.setTitle(movie.getTitle());
        dto.setYear(movie.getYear());
        dto.setGenre(movie.getGenre());
        dto.setPlot(movie.getPlot());
        dto.setPoster(movie.getPoster());
        dto.setImdbRating(movie.getImdbRating());
        dto.setDirector(movie.getDirector());
        dto.setRuntime(movie.getRuntime());
        dto.setMatchScore(matchScore);
        dto.setAiExplanation(explanation);

        if (movie.getGenre() != null) {
            dto.setGenres(Arrays.asList(movie.getGenre().split(",\\s*")));
        }

        return dto;
    }
}