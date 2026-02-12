package com.movie.repository;

import com.movie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    @Query("SELECT m FROM Movie m WHERE LOWER(m.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
    List<Movie> findByGenreContaining(@Param("genre") String genre);

    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query(value = "SELECT * FROM movies ORDER BY RANDOM() LIMIT 10", nativeQuery = true)
    List<Movie> findRandomMovies();
}