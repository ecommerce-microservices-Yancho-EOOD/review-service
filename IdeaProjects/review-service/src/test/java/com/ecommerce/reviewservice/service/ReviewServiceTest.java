package com.ecommerce.reviewservice.service;

import com.ecommerce.reviewservice.client.CustomerClient;
import com.ecommerce.reviewservice.client.ProductClient;
import com.ecommerce.reviewservice.dto.CustomerResponse;
import com.ecommerce.reviewservice.dto.ProductResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock private ReviewRepository reviewRepository;
    @Mock private CustomerClient customerClient;
    @Mock private ProductClient productClient;

    @InjectMocks
    private ReviewService reviewService;

    private UUID reviewId;
    private UUID productId;
    private UUID customerId;
    private Review review;
    private ReviewRequest reviewRequest;
    private CustomerResponse customer;
    private ProductResponse product;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        productId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        customer = CustomerResponse.builder()
                .id(customerId)
                .firstName("John")
                .email("john@test.com")
                .isActive(true)
                .build();

        product = ProductResponse.builder()
                .id(productId)
                .name("Laptop")
                .isActive(true)
                .build();

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

    // ============ CREATE TESTS ============

    @Test
    @DisplayName("Should create review when customer and product are valid")
    void createReview_ShouldSucceed() {
        when(customerClient.getCustomer(customerId)).thenReturn(customer);
        when(productClient.getProduct(productId)).thenReturn(product);
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(reviewRequest);

        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        verify(customerClient, times(1)).getCustomer(customerId);
        verify(productClient, times(1)).getProduct(productId);
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when customer not found")
    void createReview_ShouldThrowException_WhenCustomerNotFound() {
        when(customerClient.getCustomer(customerId))
                .thenThrow(new RuntimeException("Customer not found"));

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot verify customer");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when customer is inactive")
    void createReview_ShouldThrowException_WhenCustomerInactive() {
        customer.setIsActive(false);
        when(customerClient.getCustomer(customerId)).thenReturn(customer);

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inactive");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when product not found")
    void createReview_ShouldThrowException_WhenProductNotFound() {
        when(customerClient.getCustomer(customerId)).thenReturn(customer);
        when(productClient.getProduct(productId))
                .thenThrow(new RuntimeException("Product not found"));

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot verify product");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when product is inactive")
    void createReview_ShouldThrowException_WhenProductInactive() {
        product.setIsActive(false);
        when(customerClient.getCustomer(customerId)).thenReturn(customer);
        when(productClient.getProduct(productId)).thenReturn(product);

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should throw when duplicate review")
    void createReview_ShouldThrowException_WhenDuplicate() {
        when(customerClient.getCustomer(customerId)).thenReturn(customer);
        when(productClient.getProduct(productId)).thenReturn(product);
        when(reviewRepository.existsByProductIdAndCustomerId(productId, customerId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(reviewRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already reviewed");

        verify(reviewRepository, never()).save(any(Review.class));
    }

    // ============ READ TESTS ============

    @Test
    @DisplayName("Should return review when exists")
    void getReview_ShouldReturnReview() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReview(reviewId);

        assertThat(response.getId()).isEqualTo(reviewId);
    }

    @Test
    @DisplayName("Should throw when review not found")
    void getReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReview(reviewId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should return all reviews")
    void getAllReviews_ShouldReturnList() {
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        assertThat(reviewService.getAllReviews()).hasSize(1);
    }

    @Test
    @DisplayName("Should return reviews by product")
    void getReviewsByProduct_ShouldReturnList() {
        when(reviewRepository.findByProductId(productId)).thenReturn(List.of(review));

        assertThat(reviewService.getReviewsByProduct(productId)).hasSize(1);
    }

    @Test
    @DisplayName("Should return reviews by customer")
    void getReviewsByCustomer_ShouldReturnList() {
        when(reviewRepository.findByCustomerId(customerId)).thenReturn(List.of(review));

        assertThat(reviewService.getReviewsByCustomer(customerId)).hasSize(1);
    }

    // ============ ANALYTICS TESTS ============

    @Test
    @DisplayName("Should return average rating")
    void getAverageRating_ShouldReturnAverage() {
        when(reviewRepository.getAverageRating(productId)).thenReturn(4.5);

        assertThat(reviewService.getAverageRating(productId)).isEqualTo(4.5);
    }

    @Test
    @DisplayName("Should return 0.0 when no reviews")
    void getAverageRating_ShouldReturnZero_WhenNull() {
        when(reviewRepository.getAverageRating(productId)).thenReturn(null);

        assertThat(reviewService.getAverageRating(productId)).isEqualTo(0.0);
    }

    // ============ UPDATE TESTS ============

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

    // ============ DELETE TESTS ============

    @Test
    @DisplayName("Should delete review successfully")
    void deleteReview_ShouldSucceed() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
        doNothing().when(reviewRepository).delete(review);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("Should throw when deleting non-existent review")
    void deleteReview_ShouldThrowException_WhenNotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.deleteReview(reviewId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}