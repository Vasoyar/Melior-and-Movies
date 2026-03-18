package com.movie.repository;

import com.movie.model.Collection;
import com.movie.model.CollectionLike;
import com.movie.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CollectionLikeRepository extends JpaRepository<CollectionLike, Long> {

    Optional<CollectionLike> findByUserIdAndCollectionId(Long userId, Long collectionId);

    boolean existsByUserIdAndCollectionId(Long userId, Long collectionId);

    @Query("SELECT COUNT(cl) FROM CollectionLike cl WHERE cl.collection.id = :collectionId")
    long countLikesByCollectionId(@Param("collectionId") Long collectionId);

    Page<CollectionLike> findByUserId(Long userId, Pageable pageable);
}