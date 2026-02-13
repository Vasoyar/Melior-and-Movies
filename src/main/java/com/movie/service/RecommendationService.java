package com.movie.service;

import com.movie.model.dto.MovieDTO;
import com.movie.model.Movie;
import com.movie.model.UserPreference;
import com.movie.repository.MovieRepository;
import com.movie.repository.UserPreferenceRepository;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class RecommendationService {

    private final MovieRepository movieRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final OmdbService omdbService;

    private final List<String> popularMovies = Arrays.asList(
            "tt0133093", "tt0468569", "tt1375666", "tt0111161",
            "tt0109830", "tt0120737", "tt0167260", "tt0167261"
    );

    public RecommendationService(MovieRepository movieRepository,
                                 UserPreferenceRepository preferenceRepository,
                                 OmdbService omdbService) {
        this.movieRepository = movieRepository;
        this.preferenceRepository = preferenceRepository;
        this.omdbService = omdbService;
    }

    public MovieDTO getNextRecommendation(Long userId, String context) {
        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(null);

        List<Movie> movies = getAvailableMovies();

        if (movies.isEmpty()) {
            movies = loadPopularMovies();
        }

        if (movies.isEmpty()) {
            return null;
        }

        // Простой алгоритм: выбираем случайный фильм
        Random random = new Random();
        Movie selected = movies.get(random.nextInt(movies.size()));

        double matchScore = calculateMatchScore(selected, preference, context);
        String explanation = generateExplanation(selected, preference, matchScore, context);

        return omdbService.convertToDTO(selected, matchScore, explanation);
    }

    private List<Movie> getAvailableMovies() {
        List<Movie> movies = movieRepository.findAll();
        if (movies.size() < 5) {
            movies = movieRepository.findRandomMovies();
        }
        return movies;
    }

    private List<Movie> loadPopularMovies() {
        List<Movie> movies = new ArrayList<>();
        for (String imdbId : popularMovies) {
            try {
                Movie movie = omdbService.getMovieById(imdbId);
                if (movie != null) {
                    movies.add(movie);
                    movieRepository.save(movie);
                }
            } catch (Exception e) {
                System.err.println("Ошибка загрузки фильма " + imdbId);
            }
        }
        return movies;
    }

    private double calculateMatchScore(Movie movie, UserPreference pref, String context) {
        if (pref == null) return 0.7;

        double score = 0.5;

        if (movie.getGenre() != null) {
            String genre = movie.getGenre().toLowerCase();

            if (genre.contains("action")) score += pref.getActionPref() * 0.3;
            if (genre.contains("comedy")) score += pref.getComedyPref() * 0.3;
            if (genre.contains("drama")) score += pref.getDramaPref() * 0.3;
            if (genre.contains("thriller")) score += pref.getThrillerPref() * 0.3;
            if (genre.contains("sci-fi")) score += pref.getScifiPref() * 0.3;
        }

        try {
            if (movie.getImdbRating() != null && !movie.getImdbRating().equals("N/A")) {
                double rating = Double.parseDouble(movie.getImdbRating());
                score += (rating / 10) * 0.2;
            }
        } catch (NumberFormatException e) {

        }

        return Math.min(1.0, Math.max(0.0, score));
    }

    private String generateExplanation(Movie movie, UserPreference pref, double score, String context) {
        List<String> reasons = new ArrayList<>();

        if (score > 0.8) {
            reasons.add("Отлично соответствует вашим предпочтениям");
        } else if (score > 0.6) {
            reasons.add("Хорошо подходит для вас");
        }

        if (movie.getImdbRating() != null && !movie.getImdbRating().equals("N/A")) {
            reasons.add("Рейтинг IMDB: " + movie.getImdbRating() + "/10");
        }

        if (context != null) {
            if (context.equals("evening")) {
                reasons.add("Идеально для вечернего просмотра");
            } else if (context.equals("rainy")) {
                reasons.add("Создаст нужную атмосферу");
            }
        }

        return reasons.isEmpty() ? "Мы думаем, вам понравится!" : String.join(", ", reasons);
    }

    public void processSwipe(Long userId, String movieId, boolean liked) {
        System.out.println("User " + userId + " " + (liked ? "liked" : "disliked") + " movie " + movieId);
    }
}