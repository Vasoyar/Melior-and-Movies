package com.movie.repository;

import com.movie.model.Movie;
import com.movie.model.UserSwipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserSwipeRepository extends JpaRepository<UserSwipe, Long> {

    List<UserSwipe> findByUserId(Long userId);

    @Query("SELECT m FROM Movie m WHERE m.imdbId NOT IN " +
            "(SELECT s.movie.imdbId FROM UserSwipe s WHERE s.user.id = :userId)")
    List<Movie> findMoviesNotSwipedByUser(@Param("userId") Long userId);
}