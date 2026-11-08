package com.retail.wishlist.dto;

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
public class WishlistItemResponse {
    private Long id;
    private Long customerId;
    private String productSku;
    private String productName;
    private BigDecimal unitPrice;
    private String imageUrl;
    private Instant addedAt;
}

