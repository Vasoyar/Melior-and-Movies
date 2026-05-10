package com.movie.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.dto.MovieDTO;
import com.movie.dto.MovieListItemDTO;
import com.movie.model.Movie;
import com.movie.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OmdbService {

    @Value("${omdb.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MovieRepository movieRepository;

    public OmdbService(WebClient omdbWebClient, MovieRepository movieRepository) {
        this.webClient = omdbWebClient;
        this.objectMapper = new ObjectMapper();
        this.movieRepository = movieRepository;
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public Movie getMovieById(String imdbId) {

        Optional<Movie> existing = movieRepository.findById(imdbId);
        if (existing.isPresent()) {
            System.out.println("Фильм найден в БД: " + imdbId);
            return existing.get();
        }


        try {
            System.out.println("Загрузка из OMDB: " + imdbId);
            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("i", imdbId)
                            .queryParam("apikey", apiKey)
                            .queryParam("plot", "full")  // полное описание для детальной страницы
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            Movie movie = objectMapper.readValue(response, Movie.class);


            movieRepository.save(movie);
            System.out.println("Фильм сохранён в БД: " + movie.getTitle());

            return movie;

        } catch (WebClientResponseException e) {
            System.err.println("HTTP ошибка: " + e.getStatusCode());
            return null;
        } catch (Exception e) {
            System.err.println("Ошибка парсинга: " + e.getMessage());
            return null;
        }
    }


    public List<MovieListItemDTO> searchMovies(String query) {
        try {
            System.out.println("Поиск в OMDB: " + query);

            String searchResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("s", query)
                            .queryParam("apikey", apiKey)
                            .queryParam("type", "movie")
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(searchResponse);
            List<MovieListItemDTO> result = new ArrayList<>();

            if (root.has("Search")) {
                for (JsonNode node : root.get("Search")) {
                    String imdbId = node.get("imdbID").asText();
                    String title = node.get("Title").asText();
                    String year = node.get("Year").asText();
                    String poster = node.get("Poster").asText();


                    Movie movie = new Movie();
                    movie.setImdbId(imdbId);
                    movie.setTitle(title);
                    movie.setYear(year);
                    movie.setPoster(poster);

                    if (movieRepository.findById(imdbId).isEmpty()) {
                        movieRepository.save(movie);
                        System.out.println("Сохранён базовый фильм: " + title);
                    }


                    MovieListItemDTO dto = new MovieListItemDTO(imdbId, title, year, poster, null);
                    result.add(dto);
                }
            }

            return result;

        } catch (Exception e) {
            System.err.println("Ошибка поиска: " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public MovieDTO getFullMovieInfo(String imdbId) {
        Movie movie = getMovieById(imdbId);
        if (movie == null) return null;
        return convertToDTO(movie, null, null);
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
        return dto;
    }

    public boolean testOmdbConnection() {
        try {
            Movie movie = getMovieById("tt0133093");
            return movie != null;
        } catch (Exception e) {
            return false;
        }
    }
}