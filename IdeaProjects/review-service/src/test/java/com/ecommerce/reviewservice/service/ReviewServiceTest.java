package com.ecommerce.reviewservice.service;

import com.ecommerce.reviewservice.dto.ReviewRequest;
import com.ecommerce.reviewservice.dto.ReviewResponse;
import com.ecommerce.reviewservice.entity.Review;
import com.ecommerce.reviewservice.exception.BusinessException;
import com.ecommerce.reviewservice.exception.ResourceNotFoundException;
import com.ecommerce.reviewservice.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewId;
    private UUID productId;
    private UUID customerId;
    private Review review;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        productId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        review = Review.builder()
                .id(reviewId)
                .productId(productId)
                .customerId(customerId)
                .rating(5)
                .comment("Excellent product!")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reviewRequest = ReviewRequest.builder()
                .productId(productId)
                .customerId(customerId)
                .rating(5)
                .comment("Excellent product!")
                .build();
    }

    @Test
    @DisplayName("Should create review successfully")
    void createReview_ShouldSucceed() {
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(reviewRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComment()).isEqualTo("Excellent product!");
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw exception when duplicate review")
    void createReview_ShouldThrowException_WhenDuplicate() {
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");
    }

    @Test
    @DisplayName("Should return review when exists")
    void getReview_ShouldReturnReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReview(reviewId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(reviewId);
    }

    @Test
    @DisplayName("Should throw exception when review not found")
    void getReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(reviewId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return all reviews")
    void getAllReviews_ShouldReturnList() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getAllReviews();

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("Should return reviews by product")
    void getReviewsByProduct_ShouldReturnList() {
        when(reviewRepository.findByProductId(productId)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getReviewsByProduct(productId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("Should return reviews by customer")
    void getReviewsByCustomer_ShouldReturnList() {
        when(reviewRepository.findByCustomerId(customerId)).thenReturn(List.of(review));

        List<ReviewResponse> responses = reviewService.getReviewsByCustomer(customerId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCustomerId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("Should return average rating")
    void getAverageRating_ShouldReturnAverage() {
        when(reviewRepository.getAverageRating(productId)).thenReturn(4.5);

        Double avg = reviewService.getAverageRating(productId);

        assertThat(avg).isEqualTo(4.5);
    }

    @Test
    @DisplayName("Should return 0.0 when no reviews")
    void getAverageRating_ShouldReturnZero_WhenNoReviews() {
        when(reviewRepository.getAverageRating(productId)).thenReturn(null);

        Double avg = reviewService.getAverageRating(productId);

        assertThat(avg).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should update review successfully")
    void updateReview_ShouldSucceed() {
        ReviewRequest updateRequest = ReviewRequest.builder()
                .rating(3)
                .comment("Changed my mind")
                .build();

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        reviewService.updateReview(reviewId, updateRequest);

        assertThat(review.getRating()).isEqualTo(3);
        assertThat(review.getComment()).isEqualTo("Changed my mind");
    }

    @Test
    @DisplayName("Should delete review successfully")
    void deleteReview_ShouldSucceed() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent review")
    void deleteReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}