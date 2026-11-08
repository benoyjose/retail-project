package com.retail.wishlist.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.wishlist.dto.WishlistItemRequest;
import com.retail.wishlist.dto.WishlistItemResponse;
import com.retail.wishlist.dto.WishlistResponse;
import com.retail.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "Endpoints for managing customer wishlists and move-to-cart (Use Case 10)")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get customer wishlist", description = "Retrieves all wishlist items for the authenticated customer")
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId) {
        WishlistResponse wishlist = wishlistService.getWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.ok(wishlist, "Wishlist retrieved successfully"));
    }

    @PostMapping("/items")
    @Operation(summary = "Add item to wishlist", description = "Adds a product to customer wishlist")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addItem(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId,
            @Valid @RequestBody WishlistItemRequest request) {
        WishlistItemResponse item = wishlistService.addItem(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(item, "Item added to wishlist successfully"));
    }

    @DeleteMapping("/items/{sku}")
    @Operation(summary = "Remove item from wishlist", description = "Removes a product from customer wishlist by SKU")
    public ResponseEntity<ApiResponse<Void>> removeItem(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId,
            @PathVariable("sku") String sku) {
        wishlistService.removeItem(customerId, sku);
        return ResponseEntity.ok(ApiResponse.ok(null, "Item removed from wishlist successfully"));
    }

    @DeleteMapping
    @Operation(summary = "Clear wishlist", description = "Removes all items from customer wishlist")
    public ResponseEntity<ApiResponse<Void>> clearWishlist(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId) {
        wishlistService.clearWishlist(customerId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Wishlist cleared successfully"));
    }

    @PostMapping("/items/{sku}/move-to-cart")
    @Operation(summary = "Move wishlist item to shopping cart", description = "Adds item to cart and removes from wishlist")
    public ResponseEntity<ApiResponse<Void>> moveToCart(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long customerId,
            @PathVariable("sku") String sku) {
        wishlistService.moveToCart(customerId, sku);
        return ResponseEntity.ok(ApiResponse.ok(null, "Item moved to cart successfully"));
    }
}

