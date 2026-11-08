package com.retail.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistResponse {
    private Long customerId;
    @Builder.Default
    private List<WishlistItemResponse> items = new ArrayList<>();
    private int totalItems;
}

