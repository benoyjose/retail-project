package com.retail.review.service;

import com.retail.review.dto.ProductReviewSummary;
import com.retail.review.dto.ReviewCreateRequest;
import com.retail.review.dto.ReviewResponse;

public interface ReviewService {
    ReviewResponse submitReview(Long customerId, ReviewCreateRequest request);
    ProductReviewSummary getProductReviews(String productSku);
}

