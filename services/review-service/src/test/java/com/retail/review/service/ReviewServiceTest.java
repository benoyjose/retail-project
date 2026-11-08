package com.retail.review.service;

import com.retail.common.dto.ApiResponse;
import com.retail.review.dto.ProductReviewSummary;
import com.retail.review.dto.ReviewCreateRequest;
import com.retail.review.dto.ReviewResponse;
import com.retail.review.entity.Review;
import com.retail.review.exception.ReviewAlreadyExistsException;
import com.retail.review.exception.UnverifiedPurchaseException;
import com.retail.review.feign.OrderFeignClient;
import com.retail.review.repository.ReviewRepository;
import com.retail.review.service.impl.ReviewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private OrderFeignClient orderFeignClient;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Review testReview;

    @BeforeEach
    void setUp() {
        testReview = Review.builder()
                .id(1L)
                .customerId(10L)
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Amazing laptop!")
                .verifiedPurchase(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully submit review for verified purchase")
    void shouldSubmitReviewSuccessfully() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Amazing laptop!")
                .build();

        when(reviewRepository.findByCustomerIdAndProductSku(10L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.empty());
        when(orderFeignClient.verifyPurchase(10L, "SKU-LAPTOP-01"))
                .thenReturn(ApiResponse.ok(true));
        when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

        ReviewResponse response = reviewService.submitReview(10L, request);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertTrue(response.isVerifiedPurchase());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Should reject review when purchase is unverified")
    void shouldRejectUnverifiedReview() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .rating(4)
                .reviewText("Looks nice.")
                .build();

        when(reviewRepository.findByCustomerIdAndProductSku(10L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.empty());
        when(orderFeignClient.verifyPurchase(10L, "SKU-LAPTOP-01"))
                .thenReturn(ApiResponse.ok(false));

        assertThrows(UnverifiedPurchaseException.class, () -> reviewService.submitReview(10L, request));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Should reject duplicate review by same customer")
    void shouldRejectDuplicateReview() {
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .rating(5)
                .reviewText("Review again.")
                .build();

        when(reviewRepository.findByCustomerIdAndProductSku(10L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.of(testReview));

        assertThrows(ReviewAlreadyExistsException.class, () -> reviewService.submitReview(10L, request));
    }

    @Test
    @DisplayName("Should compute aggregate rating correctly")
    void shouldComputeAverageRating() {
        Review r1 = Review.builder().rating(5).reviewText("A").productSku("SKU-LAPTOP-01").build();
        Review r2 = Review.builder().rating(4).reviewText("B").productSku("SKU-LAPTOP-01").build();

        when(reviewRepository.findByProductSkuOrderByCreatedAtDesc("SKU-LAPTOP-01"))
                .thenReturn(List.of(r1, r2));

        ProductReviewSummary summary = reviewService.getProductReviews("SKU-LAPTOP-01");

        assertNotNull(summary);
        assertEquals(2, summary.getTotalReviews());
        // (5 + 4) / 2 = 4.5
        assertEquals(4.5, summary.getAverageRating());
    }
}

