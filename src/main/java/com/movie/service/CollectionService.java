package com.movie.service;

import com.movie.model.Collection;
import com.movie.model.Movie;
import com.movie.model.User;
import com.movie.repository.CollectionRepository;
import com.movie.repository.MovieRepository;
import com.movie.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final OmdbService omdbService;

    public CollectionService(CollectionRepository collectionRepository,
                             UserRepository userRepository,
                             MovieRepository movieRepository,
                             OmdbService omdbService) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.omdbService = omdbService;
    }

    public Collection createCollection(Long userId, String title, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Collection collection = new Collection(title, description, user);
        return collectionRepository.save(collection);
    }

    @Transactional
    public String addMovieToCollection(Long collectionId, String movieId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        Movie movie = movieRepository.findById(movieId)
                .orElseGet(() -> {
                    Movie newMovie = omdbService.getMovieById(movieId);
                    if (newMovie == null) {
                        throw new RuntimeException("Movie not found");
                    }
                    return movieRepository.save(newMovie);
                });

        collection.getMovies().add(movie);
        collectionRepository.save(collection);

        return "Movie added successfully";
    }

    public List<Collection> getUserCollections(Long userId) {
        return collectionRepository.findByUserId(userId);
    }
}