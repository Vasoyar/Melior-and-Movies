package com.movie.service;

import com.movie.model.Collection;
import com.movie.model.CollectionLike;
import com.movie.model.Movie;
import com.movie.model.User;
import com.movie.repository.CollectionLikeRepository;
import com.movie.repository.CollectionRepository;
import com.movie.repository.MovieRepository;
import com.movie.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final OmdbService omdbService;
    private final CollectionLikeRepository collectionLikeRepository;

    public CollectionService(
            CollectionRepository collectionRepository,
            UserRepository userRepository,
            MovieRepository movieRepository,
            OmdbService omdbService,
            CollectionLikeRepository collectionLikeRepository) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.omdbService = omdbService;
        this.collectionLikeRepository = collectionLikeRepository;
    }

    public Collection createCollection(Long userId, String title, String description) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        Collection collection = new Collection();
        collection.setTitle(title);
        collection.setDescription(description);
        collection.setUser(user);

        return collectionRepository.save(collection);
    }

    public Page<Collection> getUserCollections(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return collectionRepository.findByUserId(userId, pageable);
    }

    @Transactional
    public String addMovieToCollection(Long collectionId, String movieId, Long userId) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't own this collection");
        }

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

        collection.getMovies().add(movie);
        collectionRepository.save(collection);

        return "Movie added to collection successfully";
    }

    @Transactional
    public boolean deleteCollection(Long id, Long userId) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + id));

        if (!collection.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't own this collection");
        }

        collectionRepository.delete(collection);
        return true;
    }



    @Transactional
    public String likeCollection(Long userId, Long collectionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (collectionLikeRepository.existsByUserIdAndCollectionId(userId, collectionId)) {
            throw new RuntimeException("You already liked this collection");
        }

        CollectionLike like = new CollectionLike();
        like.setUser(user);
        like.setCollection(collection);
        like.setLikedAt(LocalDateTime.now());
        collectionLikeRepository.save(like);

        return "Collection liked successfully";
    }

    @Transactional
    public String unlikeCollection(Long userId, Long collectionId) {
        CollectionLike like = collectionLikeRepository
                .findByUserIdAndCollectionId(userId, collectionId)
                .orElseThrow(() -> new RuntimeException("You haven't liked this collection"));

        collectionLikeRepository.delete(like);

        return "Collection unliked successfully";
    }

    public long getCollectionLikesCount(Long collectionId) {
        return collectionLikeRepository.countLikesByCollectionId(collectionId);
    }

    public Page<Collection> getLikedCollections(Long userId, Pageable pageable) {
        Page<CollectionLike> likes = collectionLikeRepository.findByUserId(userId, pageable);
        return likes.map(CollectionLike::getCollection);
    }
}