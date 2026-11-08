package com.retail.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private Long customerId;
    private String productSku;
    private Integer rating;
    private String reviewText;
    private boolean verifiedPurchase;
    private Instant createdAt;
}

