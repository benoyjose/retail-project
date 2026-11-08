package com.retail.cart.service;

import com.retail.common.dto.ApiResponse;
import com.retail.cart.dto.CartItemRequest;
import com.retail.cart.dto.CartItemUpdateRequest;
import com.retail.cart.dto.CartResponse;
import com.retail.cart.dto.ProductDto;
import com.retail.cart.entity.CartItem;
import com.retail.cart.feign.ProductFeignClient;
import com.retail.cart.repository.CartItemRepository;
import com.retail.cart.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartItem existingCartItem;

    @BeforeEach
    void setUp() {
        existingCartItem = CartItem.builder()
                .id(1L)
                .customerId(100L)
                .productSku("SKU-LAPTOP-01")
                .productName("Developer Laptop")
                .unitPrice(new BigDecimal("1000.00"))
                .quantity(1)
                .subtotal(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    @DisplayName("Should add new item and calculate 8% estimated tax and grandTotal")
    void shouldAddNewItemToCart() {
        CartItemRequest request = CartItemRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .quantity(1)
                .build();

        ProductDto productDto = ProductDto.builder()
                .sku("SKU-LAPTOP-01")
                .name("Developer Laptop")
                .basePrice(new BigDecimal("1000.00"))
                .promotionalPrice(new BigDecimal("1000.00"))
                .active(true)
                .build();

        when(cartItemRepository.findByCustomerIdAndProductSku(100L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.empty());
        when(productFeignClient.getProductBySku("SKU-LAPTOP-01"))
                .thenReturn(ApiResponse.ok(productDto));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);
        when(cartItemRepository.findByCustomerId(100L)).thenReturn(List.of(existingCartItem));

        CartResponse response = cartService.addItem(100L, request);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(new BigDecimal("1000.00"), response.getSubtotal());
        // 8% tax on 1000 = 80.00
        assertEquals(new BigDecimal("80.00"), response.getEstimatedTax());
        // Grand total = 1080.00
        assertEquals(new BigDecimal("1080.00"), response.getGrandTotal());
    }

    @Test
    @DisplayName("Should increment quantity when adding already existing item")
    void shouldIncrementExistingItemQuantity() {
        CartItemRequest request = CartItemRequest.builder()
                .productSku("SKU-LAPTOP-01")
                .quantity(2)
                .build();

        when(cartItemRepository.findByCustomerIdAndProductSku(100L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);
        when(cartItemRepository.findByCustomerId(100L)).thenReturn(List.of(existingCartItem));

        CartResponse response = cartService.addItem(100L, request);

        assertNotNull(response);
        assertEquals(3, existingCartItem.getQuantity());
        verify(productFeignClient, never()).getProductBySku(anyString());
    }

    @Test
    @DisplayName("Should update item quantity")
    void shouldUpdateItemQuantity() {
        CartItemUpdateRequest updateRequest = CartItemUpdateRequest.builder()
                .quantity(5)
                .build();

        when(cartItemRepository.findByCustomerIdAndProductSku(100L, "SKU-LAPTOP-01"))
                .thenReturn(Optional.of(existingCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(existingCartItem);
        when(cartItemRepository.findByCustomerId(100L)).thenReturn(List.of(existingCartItem));

        CartResponse response = cartService.updateQuantity(100L, "SKU-LAPTOP-01", updateRequest);

        assertNotNull(response);
        assertEquals(5, existingCartItem.getQuantity());
    }
}

