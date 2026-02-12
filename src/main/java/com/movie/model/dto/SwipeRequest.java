package com.movie.model.dto;

public class SwipeRequest {

    private String movieId;
    private Boolean liked;

    // Конструкторы
    public SwipeRequest() {}

    public SwipeRequest(String movieId, Boolean liked) {
        this.movieId = movieId;
        this.liked = liked;
    }

    // Геттеры и Сеттеры
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
}