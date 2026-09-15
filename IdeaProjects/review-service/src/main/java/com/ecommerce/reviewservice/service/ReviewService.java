package com.ecommerce.reviewservice.service;

import com.ecommerce.reviewservice.dto.ReviewRequest;
import com.ecommerce.reviewservice.dto.ReviewResponse;
import com.ecommerce.reviewservice.entity.Review;
import com.ecommerce.reviewservice.exception.BusinessException;
import com.ecommerce.reviewservice.exception.ResourceNotFoundException;
import com.ecommerce.reviewservice.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // ============ CREATE ============
    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        log.info("Creating review for product: {} by customer: {}",
                request.getProductId(), request.getCustomerId());

        // Check if customer already reviewed this product
        if (reviewRepository.existsByProductIdAndCustomerId(
                request.getProductId(), request.getCustomerId())) {
            throw new BusinessException(
                    "Customer has already reviewed this product", "DUPLICATE_REVIEW");
        }

        Review review = Review.builder()
                .productId(request.getProductId())
                .customerId(request.getCustomerId())
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review created with ID: {}", saved.getId());
        return toResponse(saved);
    }

    // ============ READ ============
    public ReviewResponse getReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        return toResponse(review);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewResponse> getReviewsByProduct(UUID productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewResponse> getReviewsByCustomer(UUID customerId) {
        return reviewRepository.findByCustomerId(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    // ============ ANALYTICS ============
    public Double getAverageRating(UUID productId) {
        Double avg = reviewRepository.getAverageRating(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public Map<String, Object> getProductReviewStats(UUID productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productId", productId);
        stats.put("averageRating", getAverageRating(productId));
        stats.put("totalReviews", reviewRepository.countByProductId(productId));

        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, reviewRepository.countByProductIdAndRating(productId, i));
        }
        stats.put("ratingDistribution", distribution);

        return stats;
    }

    // ============ UPDATE ============
    @Transactional
    public ReviewResponse updateReview(UUID id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));

        if (request.getRating() != null) review.setRating(request.getRating());
        if (request.getComment() != null) review.setComment(request.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        return toResponse(reviewRepository.save(review));
    }

    // ============ DELETE ============
    @Transactional
    public void deleteReview(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", id));
        reviewRepository.delete(review);
        log.info("Review deleted: {}", id);
    }

    // ============ MAPPING ============
    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .customerId(review.getCustomerId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}