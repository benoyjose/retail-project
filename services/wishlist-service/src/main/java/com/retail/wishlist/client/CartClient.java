package com.retail.wishlist.client;

import com.retail.common.dto.ApiResponse;
import com.retail.wishlist.dto.CartItemAddRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service")
public interface CartClient {

    @PostMapping("/api/v1/cart/items")
    ApiResponse<Object> addItemToCart(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CartItemAddRequest request);
}

