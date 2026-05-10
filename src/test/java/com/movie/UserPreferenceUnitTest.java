package com.movie;

import com.movie.model.UserPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserPreferenceUnitTest {

    private UserPreference preference;

    @BeforeEach
    void setUp() {
        preference = new UserPreference();
        preference.setActionPref(0.5);
        preference.setComedyPref(0.5);
        preference.setDramaPref(0.5);
    }

    @Test
    @DisplayName("Лайк Action фильма увеличивает actionPref на 0.1")
    void likeAction() {
        preference.updatePreferences("Action, Thriller", true);
        assertThat(preference.getActionPref()).isEqualTo(0.6);
        assertThat(preference.getComedyPref()).isEqualTo(0.5);
    }

    @Test
    @DisplayName("Дизлайк Drama фильма уменьшает dramaPref на 0.1")
    void dislikeDrama() {
        preference.updatePreferences("Drama", false);
        assertThat(preference.getDramaPref()).isEqualTo(0.4);
    }

    @Test
    @DisplayName("Значение предпочтения не может стать меньше 0 или больше 1")
    void boundaryValues() {
        preference.setActionPref(0.95);
        preference.updatePreferences("Action", true);
        assertThat(preference.getActionPref()).isEqualTo(1.0);

        preference.setActionPref(0.05);
        preference.updatePreferences("Action", false);
        assertThat(preference.getActionPref()).isEqualTo(0.0);
    }
}
