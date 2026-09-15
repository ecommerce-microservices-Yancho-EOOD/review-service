package com.ecommerce.reviewservice.repository;

import com.ecommerce.reviewservice.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByProductId(UUID productId);

    List<Review> findByCustomerId(UUID customerId);

    Optional<Review> findByProductIdAndCustomerId(UUID productId, UUID customerId);

    boolean existsByProductIdAndCustomerId(UUID productId, UUID customerId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRating(@Param("productId") UUID productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.rating = :rating")
    long countByProductIdAndRating(@Param("productId") UUID productId, @Param("rating") Integer rating);

    long countByProductId(UUID productId);
}