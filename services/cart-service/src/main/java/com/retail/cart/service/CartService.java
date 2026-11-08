package com.retail.cart.service;

import com.retail.cart.dto.CartItemRequest;
import com.retail.cart.dto.CartItemUpdateRequest;
import com.retail.cart.dto.CartResponse;

public interface CartService {
    CartResponse getCart(Long customerId);
    CartResponse addItem(Long customerId, CartItemRequest request);
    CartResponse updateQuantity(Long customerId, String sku, CartItemUpdateRequest request);
    CartResponse removeItem(Long customerId, String sku);
    void clearCart(Long customerId);
}

