package com.retail.cart.service.impl;

import com.retail.common.dto.ApiResponse;
import com.retail.cart.dto.*;
import com.retail.cart.entity.CartItem;
import com.retail.cart.exception.CartItemNotFoundException;
import com.retail.cart.feign.ProductFeignClient;
import com.retail.cart.repository.CartItemRepository;
import com.retail.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductFeignClient productFeignClient;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.08"); // 8% sales tax

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long customerId) {
        List<CartItem> items = cartItemRepository.findByCustomerId(customerId);
        return buildCartResponse(customerId, items);
    }

    @Override
    @Transactional
    public CartResponse addItem(Long customerId, CartItemRequest request) {
        log.info("Adding item to cart for customer {}: SKU={}, Qty={}", customerId, request.getProductSku(), request.getQuantity());

        String sku = request.getProductSku().trim().toUpperCase();

        // Check if item already in cart
        Optional<CartItem> existingItemOpt = cartItemRepository.findByCustomerIdAndProductSku(customerId, sku);

        if (existingItemOpt.isPresent()) {
            CartItem existing = existingItemOpt.get();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.calculateSubtotal();
            cartItemRepository.save(existing);
        } else {
            // Lookup product price and name from product-service
            String productName = "Product " + sku;
            BigDecimal unitPrice = new BigDecimal("100.00"); // fallback

            try {
                ApiResponse<ProductDto> productResponse = productFeignClient.getProductBySku(sku);
                if (productResponse != null && productResponse.getData() != null) {
                    ProductDto product = productResponse.getData();
                    productName = product.getName();
                    unitPrice = product.getPromotionalPrice() != null ? product.getPromotionalPrice() : product.getBasePrice();
                }
            } catch (Exception e) {
                log.warn("Could not fetch product details from product-service for SKU {}. Using default fallback price.", sku);
            }

            CartItem newItem = CartItem.builder()
                    .customerId(customerId)
                    .productSku(sku)
                    .productName(productName)
                    .unitPrice(unitPrice)
                    .quantity(request.getQuantity())
                    .build();

            newItem.calculateSubtotal();
            cartItemRepository.save(newItem);
        }

        return getCart(customerId);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long customerId, String sku, CartItemUpdateRequest request) {
        String cleanSku = sku.trim().toUpperCase();
        CartItem item = cartItemRepository.findByCustomerIdAndProductSku(customerId, cleanSku)
                .orElseThrow(() -> new CartItemNotFoundException("Cart item not found for SKU: " + cleanSku));

        item.setQuantity(request.getQuantity());
        item.calculateSubtotal();
        cartItemRepository.save(item);

        return getCart(customerId);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long customerId, String sku) {
        String cleanSku = sku.trim().toUpperCase();
        cartItemRepository.deleteByCustomerIdAndProductSku(customerId, cleanSku);
        return getCart(customerId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        log.info("Clearing shopping cart for customer: {}", customerId);
        cartItemRepository.deleteByCustomerId(customerId);
    }

    private CartResponse buildCartResponse(Long customerId, List<CartItem> items) {
        List<CartItemResponse> itemResponses = items.stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productSku(item.getProductSku())
                        .productName(item.getProductName())
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        BigDecimal subtotal = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal estimatedTax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = subtotal.add(estimatedTax).setScale(2, RoundingMode.HALF_UP);

        int totalItems = items.stream().mapToInt(CartItem::getQuantity).sum();

        return CartResponse.builder()
                .customerId(customerId)
                .items(itemResponses)
                .totalItems(totalItems)
                .subtotal(subtotal)
                .estimatedTax(estimatedTax)
                .grandTotal(grandTotal)
                .build();
    }
}

