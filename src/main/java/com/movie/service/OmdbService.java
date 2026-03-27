package com.movie.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.dto.MovieDTO;
import com.movie.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OmdbService {

    @Value("${omdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OmdbService(WebClient omdbWebClient) {
        this.webClient = omdbWebClient;
        this.objectMapper = new ObjectMapper();


        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        System.out.println("OmdbService инициализирован с игнорированием неизвестных полей");
    }

    public Movie getMovieById(String imdbId) {
        try {
            System.out.println("Запрос к OMDB: фильм " + imdbId);

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("i", imdbId)
                            .queryParam("apikey", apiKey)
                            .queryParam("plot", "short")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Получен ответ от OMDB, длина: " + response.length());


            if (response.contains("\"Response\":\"False\"")) {
                System.err.println("OMDB вернул ошибку: " + response);
                return null;
            }


            Movie movie = objectMapper.readValue(response, Movie.class);
            System.out.println("Фильм: " + movie.getTitle());

            return movie;

        } catch (WebClientResponseException e) {
            System.err.println("HTTP ошибка: " + e.getStatusCode());
            System.err.println("Тело ответа: " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка парсинга: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public List<Movie> searchMovies(String query) {
        try {
            System.out.println("ПОИСК ФИЛЬМОВ: " + query);

            String searchResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("s", query)
                            .queryParam("apikey", apiKey)
                            .queryParam("type", "movie")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Ответ от OMDB (поиск): " + searchResponse);

            if (searchResponse.contains("\"Response\":\"False\"")) {
                System.err.println("OMDB вернул ошибку: " + searchResponse);
                return new ArrayList<>();
            }


            JsonNode root = objectMapper.readTree(searchResponse);
            List<Movie> movies = new ArrayList<>();

            if (root.has("Search")) {
                JsonNode searchArray = root.get("Search");
                System.out.println("Найдено результатов: " + searchArray.size());

                for (JsonNode node : searchArray) {
                    String imdbId = node.get("imdbID").asText();
                    System.out.println("Загружаю детали для: " + imdbId + " - " + node.get("Title").asText());

                    Movie movie = getMovieById(imdbId);
                    if (movie != null) {
                        movies.add(movie);
                    }
                }
                System.out.println("Успешно загружено фильмов: " + movies.size());
            } else {
                System.out.println("В ответе нет поля 'Search'");
            }

            return movies;

        } catch (WebClientResponseException e) {
            System.err.println("HTTP ошибка: " + e.getStatusCode());
            System.err.println("Тело ответа: " + e.getResponseBodyAsString());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


    public boolean testOmdbConnection() {
        try {
            Movie movie = getMovieById("tt0133093");
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