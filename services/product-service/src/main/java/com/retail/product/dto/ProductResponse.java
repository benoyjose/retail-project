package com.retail.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal promotionalPrice;
    private BigDecimal discountPercentage;
    private String campaignApplied;
    private Long categoryId;
    private String categoryName;
    private String brand;
    private String imageUrl;
    private boolean active;
    private Double averageRating;
    private Integer reviewCount;
    private Instant createdAt;
}

