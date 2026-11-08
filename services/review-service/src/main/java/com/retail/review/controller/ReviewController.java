package com.retail.review.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.review.dto.ProductReviewSummary;
import com.retail.review.dto.ReviewCreateRequest;
import com.retail.review.dto.ReviewResponse;
import com.retail.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews & Ratings", description = "Verified Customer Review & Star Rating APIs (Use Case 6)")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit a product review and 1-5 rating (Use Case 6)")
    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        Long customerId = Long.parseLong(userId);
        ReviewResponse response = reviewService.submitReview(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Review submitted successfully"));
    }

    @Operation(summary = "Get reviews and aggregate rating summary for a product")
    @GetMapping("/product/{sku}")
    public ResponseEntity<ApiResponse<ProductReviewSummary>> getProductReviews(
            @PathVariable("sku") String sku) {
        ProductReviewSummary summary = reviewService.getProductReviews(sku);
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }
}

