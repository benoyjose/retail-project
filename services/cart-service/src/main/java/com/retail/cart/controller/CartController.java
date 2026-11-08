package com.retail.cart.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.cart.dto.CartItemRequest;
import com.retail.cart.dto.CartItemUpdateRequest;
import com.retail.cart.dto.CartResponse;
import com.retail.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping Cart Management APIs (Use Case 3)")
public class CartController {

    private final CartService cartService;

    @Operation(summary = "Get current customer shopping cart")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId) {
        Long customerId = Long.parseLong(userId);
        CartResponse cart = cartService.getCart(customerId);
        return ResponseEntity.ok(ApiResponse.ok(cart));
    }

    @Operation(summary = "Add an item to the shopping cart")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @Valid @RequestBody CartItemRequest request) {
        Long customerId = Long.parseLong(userId);
        CartResponse cart = cartService.addItem(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Item added to cart"));
    }

    @Operation(summary = "Update item quantity in shopping cart")
    @PutMapping("/items/{sku}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @PathVariable("sku") String sku,
            @Valid @RequestBody CartItemUpdateRequest request) {
        Long customerId = Long.parseLong(userId);
        CartResponse cart = cartService.updateQuantity(customerId, sku, request);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Cart item quantity updated"));
    }

    @Operation(summary = "Remove an item from shopping cart")
    @DeleteMapping("/items/{sku}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @PathVariable("sku") String sku) {
        Long customerId = Long.parseLong(userId);
        CartResponse cart = cartService.removeItem(customerId, sku);
        return ResponseEntity.ok(ApiResponse.ok(cart, "Item removed from cart"));
    }

    @Operation(summary = "Clear all items in shopping cart")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId) {
        Long customerId = Long.parseLong(userId);
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Shopping cart cleared successfully"));
    }
}

