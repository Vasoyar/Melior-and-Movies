package com.movie.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences")
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "action_pref")
    private Double actionPref = 0.5;

    @Column(name = "comedy_pref")
    private Double comedyPref = 0.5;

    @Column(name = "drama_pref")
    private Double dramaPref = 0.5;

    @Column(name = "thriller_pref")
    private Double thrillerPref = 0.5;

    @Column(name = "romance_pref")
    private Double romancePref = 0.5;

    @Column(name = "scifi_pref")
    private Double scifiPref = 0.5;

    @Column(name = "horror_pref")
    private Double horrorPref = 0.5;

    // Конструкторы
    public UserPreference() {}

    public UserPreference(User user) {
        this.user = user;
    }

    // Метод обновления предпочтений
    public void updatePreferences(String genre, boolean liked) {
        double change = liked ? 0.1 : -0.1;
        String lowerGenre = genre.toLowerCase();

        if (lowerGenre.contains("action")) this.actionPref = clamp(this.actionPref + change);
        if (lowerGenre.contains("comedy")) this.comedyPref = clamp(this.comedyPref + change);
        if (lowerGenre.contains("drama")) this.dramaPref = clamp(this.dramaPref + change);
        if (lowerGenre.contains("thriller")) this.thrillerPref = clamp(this.thrillerPref + change);
        if (lowerGenre.contains("romance")) this.romancePref = clamp(this.romancePref + change);
        if (lowerGenre.contains("sci-fi") || lowerGenre.contains("scifi")) this.scifiPref = clamp(this.scifiPref + change);
        if (lowerGenre.contains("horror")) this.horrorPref = clamp(this.horrorPref + change);
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Double getActionPref() { return actionPref; }
    public void setActionPref(Double actionPref) { this.actionPref = actionPref; }

    public Double getComedyPref() { return comedyPref; }
    public void setComedyPref(Double comedyPref) { this.comedyPref = comedyPref; }

    public Double getDramaPref() { return dramaPref; }
    public void setDramaPref(Double dramaPref) { this.dramaPref = dramaPref; }

    public Double getThrillerPref() { return thrillerPref; }
    public void setThrillerPref(Double thrillerPref) { this.thrillerPref = thrillerPref; }

    public Double getRomancePref() { return romancePref; }
    public void setRomancePref(Double romancePref) { this.romancePref = romancePref; }

    public Double getScifiPref() { return scifiPref; }
    public void setScifiPref(Double scifiPref) { this.scifiPref = scifiPref; }

    public Double getHorrorPref() { return horrorPref; }
    public void setHorrorPref(Double horrorPref) { this.horrorPref = horrorPref; }
}