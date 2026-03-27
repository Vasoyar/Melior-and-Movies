package com.movie.service;

import com.movie.dto.MovieDTO;
import com.movie.dto.SwipeRequest;
import com.movie.model.Movie;
import com.movie.model.User;
import com.movie.model.UserPreference;
import com.movie.model.UserSwipe;
import com.movie.repository.MovieRepository;
import com.movie.repository.UserPreferenceRepository;
import com.movie.repository.UserRepository;
import com.movie.repository.UserSwipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class RecommendationService {

    private final MovieRepository movieRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final UserSwipeRepository swipeRepository;
    private final OmdbService omdbService;

    public RecommendationService(
            MovieRepository movieRepository,
            UserPreferenceRepository preferenceRepository,
            UserRepository userRepository,
            UserSwipeRepository swipeRepository,
            OmdbService omdbService) {
        this.movieRepository = movieRepository;
        this.preferenceRepository = preferenceRepository;
        this.userRepository = userRepository;
        this.swipeRepository = swipeRepository;
        this.omdbService = omdbService;
    }

    @Transactional
    public void processSwipe(Long userId, String movieId, boolean liked) {
        System.out.println("Обработка свайпа: пользователь " + userId +
                (liked ? " лайк" : " дизлайк") + " фильм " + movieId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));


        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserPreference newPref = new UserPreference();
                    newPref.setUser(user);
                    return preferenceRepository.save(newPref);
                });

        Movie movie = movieRepository.findById(movieId)
                .orElseGet(() -> {
                    Movie newMovie = omdbService.getMovieById(movieId);
                    if (newMovie != null) {
                        return movieRepository.save(newMovie);
                    }
                    return null;
                });

        if (movie == null) {
            throw new RuntimeException("Movie not found with id: " + movieId);
        }

        UserSwipe swipe = new UserSwipe();
        swipe.setUser(user);
        swipe.setMovie(movie);
        swipe.setLiked(liked);
        swipe.setSwipedAt(LocalDateTime.now());
        swipeRepository.save(swipe);

        if (movie.getGenre() != null) {
            String[] genres = movie.getGenre().split(",\\s*");
            for (String genre : genres) {
                updatePreferenceByGenre(preference, genre.toLowerCase(), liked);
            }
        }

        preferenceRepository.save(preference);
        System.out.println("Свайп обработан, предпочтения обновлены");
    }

    private void updatePreferenceByGenre(UserPreference pref, String genre, boolean liked) {
        double change = liked ? 0.1 : -0.1;

        if (genre.contains("action")) {
            pref.setActionPref(clamp(pref.getActionPref() + change));
        } else if (genre.contains("comedy")) {
            pref.setComedyPref(clamp(pref.getComedyPref() + change));
        } else if (genre.contains("drama")) {
            pref.setDramaPref(clamp(pref.getDramaPref() + change));
        } else if (genre.contains("thriller")) {
            pref.setThrillerPref(clamp(pref.getThrillerPref() + change));
        } else if (genre.contains("romance")) {
            pref.setRomancePref(clamp(pref.getRomancePref() + change));
        } else if (genre.contains("sci-fi") || genre.contains("scifi")) {
            pref.setScifiPref(clamp(pref.getScifiPref() + change));
        } else if (genre.contains("horror")) {
            pref.setHorrorPref(clamp(pref.getHorrorPref() + change));
        }
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    public MovieDTO getNextRecommendation(Long userId, String context) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserPreference preference = preferenceRepository.findByUserId(userId)
                .orElse(new UserPreference());

        List<Movie> unwatchedMovies = swipeRepository.findMoviesNotSwipedByUser(userId);

        if (unwatchedMovies.isEmpty()) {

            unwatchedMovies = loadPopularMovies();
        }

        if (unwatchedMovies.isEmpty()) {
            return null;
        }


        Movie bestMovie = null;
        double bestScore = -1;

        for (Movie movie : unwatchedMovies) {
            double score = calculateMatchScore(movie, preference, context);
            if (score > bestScore) {
                bestScore = score;
                bestMovie = movie;
            }
        }

        if (bestMovie == null) {
            bestMovie = unwatchedMovies.get(new Random().nextInt(unwatchedMovies.size()));
            bestScore = 0.7;
        }


        String explanation = generateExplanation(bestMovie, preference, bestScore, context);

        return omdbService.convertToDTO(bestMovie, bestScore, explanation);
    }

    private double calculateMatchScore(Movie movie, UserPreference pref, String context) {
        double score = 0.5;

        if (movie.getGenre() != null) {
            String genre = movie.getGenre().toLowerCase();

            if (genre.contains("action")) score += pref.getActionPref() * 0.3;
            if (genre.contains("comedy")) score += pref.getComedyPref() * 0.3;
            if (genre.contains("drama")) score += pref.getDramaPref() * 0.3;
            if (genre.contains("thriller")) score += pref.getThrillerPref() * 0.3;
            if (genre.contains("romance")) score += pref.getRomancePref() * 0.3;
            if (genre.contains("sci-fi")) score += pref.getScifiPref() * 0.3;
            if (genre.contains("horror")) score += pref.getHorrorPref() * 0.3;
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
        StringBuilder explanation = new StringBuilder();

        if (score > 0.8) {
            explanation.append("Отлично соответствует вашим предпочтениям. ");
        } else if (score > 0.6) {
            explanation.append("Хорошо подходит для вас. ");
        }

        if (movie.getImdbRating() != null && !movie.getImdbRating().equals("N/A")) {
            explanation.append("Рейтинг IMDB: ").append(movie.getImdbRating()).append("/10. ");
        }

        if (context != null) {
            if ("evening".equals(context)) {
                explanation.append("Идеально для вечернего просмотра. ");
            } else if ("rainy".equals(context)) {
                explanation.append("Создаст нужную атмосферу. ");
            }
        }

        return explanation.toString().trim();
    }

    private List<Movie> loadPopularMovies() {

        String[] popularIds = {"tt0133093", "tt0468569", "tt1375666", "tt0111161", "tt0109830"};
        for (String id : popularIds) {
            Movie movie = omdbService.getMovieById(id);
            if (movie != null) {
                movieRepository.save(movie);
            }
        }
        return movieRepository.findRandomMovies();
    }
}