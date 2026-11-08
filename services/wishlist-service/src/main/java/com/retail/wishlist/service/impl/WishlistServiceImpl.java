package com.retail.wishlist.service.impl;

import com.retail.common.dto.ApiResponse;
import com.retail.wishlist.client.CartClient;
import com.retail.wishlist.client.ProductClient;
import com.retail.wishlist.dto.*;
import com.retail.wishlist.entity.WishlistItem;
import com.retail.wishlist.exception.DuplicateWishlistItemException;
import com.retail.wishlist.exception.WishlistItemNotFoundException;
import com.retail.wishlist.repository.WishlistRepository;
import com.retail.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductClient productClient;
    private final CartClient cartClient;

    @Override
    @Transactional(readOnly = true)
    public WishlistResponse getWishlist(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        List<WishlistItem> items = wishlistRepository.findByCustomerIdOrderByAddedAtDesc(customerId);
        List<WishlistItemResponse> responseItems = items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return WishlistResponse.builder()
                .customerId(customerId)
                .items(responseItems)
                .totalItems(responseItems.size())
                .build();
    }

    @Override
    @Transactional
    public WishlistItemResponse addItem(Long customerId, WishlistItemRequest request) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        String sku = request.getProductSku().trim().toUpperCase();

        if (wishlistRepository.existsByCustomerIdAndProductSku(customerId, sku)) {
            throw new DuplicateWishlistItemException("Product " + sku + " is already in the wishlist");
        }

        String productName = sku;
        BigDecimal unitPrice = BigDecimal.ZERO;
        String imageUrl = null;

        try {
            ApiResponse<ProductDto> productResponse = productClient.getProductBySku(sku);
            if (productResponse != null && productResponse.getData() != null) {
                ProductDto product = productResponse.getData();
                productName = product.getName();
                unitPrice = product.getPromotionalPrice() != null ? product.getPromotionalPrice() : product.getBasePrice();
                imageUrl = product.getImageUrl();
            }
        } catch (Exception ex) {
            log.warn("Could not retrieve product details for SKU {}: {}", sku, ex.getMessage());
        }

        WishlistItem item = WishlistItem.builder()
                .customerId(customerId)
                .productSku(sku)
                .productName(productName)
                .unitPrice(unitPrice)
                .imageUrl(imageUrl)
                .build();

        WishlistItem saved = wishlistRepository.save(item);
        log.info("Added SKU {} to customer {} wishlist", sku, customerId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void removeItem(Long customerId, String productSku) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        String sku = productSku.trim().toUpperCase();
        WishlistItem item = wishlistRepository.findByCustomerIdAndProductSku(customerId, sku)
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found for SKU: " + sku));

        wishlistRepository.delete(item);
        log.info("Removed SKU {} from customer {} wishlist", sku, customerId);
    }

    @Override
    @Transactional
    public void clearWishlist(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        wishlistRepository.deleteByCustomerId(customerId);
        log.info("Cleared wishlist for customer {}", customerId);
    }

    @Override
    @Transactional
    public void moveToCart(Long customerId, String productSku) {
        if (customerId == null) {
            throw new IllegalArgumentException("Customer ID is required");
        }

        String sku = productSku.trim().toUpperCase();
        WishlistItem item = wishlistRepository.findByCustomerIdAndProductSku(customerId, sku)
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found for SKU: " + sku));

        // Add to shopping cart via CartClient
        cartClient.addItemToCart(String.valueOf(customerId), CartItemAddRequest.builder()
                .productSku(sku)
                .quantity(1)
                .build());

        // Remove from wishlist
        wishlistRepository.delete(item);
        log.info("Moved SKU {} to shopping cart for customer {}", sku, customerId);
    }

    private WishlistItemResponse mapToResponse(WishlistItem item) {
        return WishlistItemResponse.builder()
                .id(item.getId())
                .customerId(item.getCustomerId())
                .productSku(item.getProductSku())
                .productName(item.getProductName())
                .unitPrice(item.getUnitPrice())
                .imageUrl(item.getImageUrl())
                .addedAt(item.getAddedAt())
                .build();
    }
}

