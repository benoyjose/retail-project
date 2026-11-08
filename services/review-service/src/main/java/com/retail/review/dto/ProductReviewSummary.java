package com.retail.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductReviewSummary {
    private String productSku;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponse> reviews;
}

