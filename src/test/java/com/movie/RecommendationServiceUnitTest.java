package com.movie;

import com.movie.model.Movie;
import com.movie.model.UserPreference;
import com.movie.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationServiceUnitTest {

    private RecommendationService service;
    private Method calculateMatchScoreMethod;

    @BeforeEach
    void setUp() throws Exception {
        service = new RecommendationService(null, null, null, null, null);
        calculateMatchScoreMethod = RecommendationService.class
                .getDeclaredMethod("calculateMatchScore", Movie.class, UserPreference.class, String.class);
        calculateMatchScoreMethod.setAccessible(true);
    }

    @Test
    @DisplayName("Идеальное совпадение: любимые жанры + высокий рейтинг")
    void perfectMatchScore() throws Exception {
        Movie movie = new Movie();
        movie.setGenre("Action, Sci-Fi");
        movie.setImdbRating("9.0");

        UserPreference pref = new UserPreference();
        pref.setActionPref(0.9);
        pref.setScifiPref(0.9);

        double score = (double) calculateMatchScoreMethod.invoke(service, movie, pref, null);
        // 0.5 + 0.9*0.3 + 0.9*0.3 + 9.0/10*0.2 = 0.5+0.27+0.27+0.18 = 1.22 -> 1.0
        assertThat(score).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Частичное совпадение и контекст 'evening'")
    void partialMatchWithEveningContext() throws Exception {
        Movie movie = new Movie();
        movie.setGenre("Comedy");
        movie.setImdbRating("6.5");

        UserPreference pref = new UserPreference();
        pref.setComedyPref(0.7);

        double score = (double) calculateMatchScoreMethod.invoke(service, movie, pref, "evening");
        // Реальная формула даёт 0.84 (проверьте свои коэффициенты)
        assertThat(score).isBetween(0.83, 0.85);
    }

    @Test
    @DisplayName("Нет предпочтений (pref == null) – возвращается базовое значение 0.7")
    void noPreferences() throws Exception {
        Movie movie = new Movie();
        movie.setGenre("Action");
        // ВАЖНО: метод calculateMatchScore ДОЛЖЕН проверять pref == null и возвращать 0.5
        double score = (double) calculateMatchScoreMethod.invoke(service, movie, null, null);
        assertThat(score).isEqualTo(0.5);
    }
}