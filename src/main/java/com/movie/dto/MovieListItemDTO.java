package com.movie.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткая информация о фильме для списка")
public class MovieListItemDTO {

    @Schema(description = "IMDb ID фильма", example = "tt0133093")
    private String imdbId;

    @Schema(description = "Название фильма", example = "The Matrix")
    private String title;

    @Schema(description = "Год выпуска", example = "1999")
    private String year;

    @Schema(description = "URL постера", example = "https://...")
    private String poster;

    @Schema(description = "Рейтинг IMDb", example = "8.7")
    private String imdbRating;

    public MovieListItemDTO() {}

    public MovieListItemDTO(String imdbId, String title, String year, String poster, String imdbRating) {
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
        this.poster = poster;
        this.imdbRating = imdbRating;
    }

    public String getImdbId() { return imdbId; }
    public void setImdbId(String imdbId) { this.imdbId = imdbId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getImdbRating() { return imdbRating; }
    public void setImdbRating(String imdbRating) { this.imdbRating = imdbRating; }
}