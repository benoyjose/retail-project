package com.retail.wishlist.service;

import com.retail.wishlist.dto.WishlistItemRequest;
import com.retail.wishlist.dto.WishlistItemResponse;
import com.retail.wishlist.dto.WishlistResponse;

public interface WishlistService {

    WishlistResponse getWishlist(Long customerId);

    WishlistItemResponse addItem(Long customerId, WishlistItemRequest request);

    void removeItem(Long customerId, String productSku);

    void clearWishlist(Long customerId);

    void moveToCart(Long customerId, String productSku);
}

