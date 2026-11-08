package com.retail.wishlist.service;

import com.retail.common.dto.ApiResponse;
import com.retail.wishlist.client.CartClient;
import com.retail.wishlist.client.ProductClient;
import com.retail.wishlist.dto.CartItemAddRequest;
import com.retail.wishlist.dto.ProductDto;
import com.retail.wishlist.dto.WishlistItemRequest;
import com.retail.wishlist.dto.WishlistItemResponse;
import com.retail.wishlist.dto.WishlistResponse;
import com.retail.wishlist.entity.WishlistItem;
import com.retail.wishlist.exception.DuplicateWishlistItemException;
import com.retail.wishlist.exception.WishlistItemNotFoundException;
import com.retail.wishlist.repository.WishlistRepository;
import com.retail.wishlist.service.impl.WishlistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private CartClient cartClient;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private WishlistItem item;

    @BeforeEach
    void setUp() {
        item = WishlistItem.builder()
                .id(1L)
                .customerId(100L)
                .productSku("SKU-PHONE-01")
                .productName("Smartphone Ultra")
                .unitPrice(BigDecimal.valueOf(999.99))
                .addedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should get wishlist for customer")
    void shouldGetWishlist() {
        when(wishlistRepository.findByCustomerIdOrderByAddedAtDesc(100L)).thenReturn(List.of(item));

        WishlistResponse response = wishlistService.getWishlist(100L);

        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isEqualTo(100L);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getTotalItems()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should add item to wishlist")
    void shouldAddItem() {
        when(wishlistRepository.existsByCustomerIdAndProductSku(100L, "SKU-PHONE-01")).thenReturn(false);

        ProductDto productDto = ProductDto.builder()
                .id(1L)
                .sku("SKU-PHONE-01")
                .name("Smartphone Ultra")
                .basePrice(BigDecimal.valueOf(999.99))
                .build();
        when(productClient.getProductBySku("SKU-PHONE-01")).thenReturn(ApiResponse.ok(productDto));
        when(wishlistRepository.save(any(WishlistItem.class))).thenReturn(item);

        WishlistItemResponse response = wishlistService.addItem(100L, new WishlistItemRequest("SKU-PHONE-01"));

        assertThat(response).isNotNull();
        assertThat(response.getProductSku()).isEqualTo("SKU-PHONE-01");
        verify(wishlistRepository, times(1)).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("Should throw DuplicateWishlistItemException if item already in wishlist")
    void shouldThrowDuplicateException() {
        when(wishlistRepository.existsByCustomerIdAndProductSku(100L, "SKU-PHONE-01")).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addItem(100L, new WishlistItemRequest("SKU-PHONE-01")))
                .isInstanceOf(DuplicateWishlistItemException.class);

        verify(wishlistRepository, never()).save(any(WishlistItem.class));
    }

    @Test
    @DisplayName("Should remove item from wishlist")
    void shouldRemoveItem() {
        when(wishlistRepository.findByCustomerIdAndProductSku(100L, "SKU-PHONE-01")).thenReturn(Optional.of(item));

        wishlistService.removeItem(100L, "SKU-PHONE-01");

        verify(wishlistRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Should move item from wishlist to cart")
    void shouldMoveToCart() {
        when(wishlistRepository.findByCustomerIdAndProductSku(100L, "SKU-PHONE-01")).thenReturn(Optional.of(item));

        wishlistService.moveToCart(100L, "SKU-PHONE-01");

        verify(cartClient, times(1)).addItemToCart(eq("100"), any(CartItemAddRequest.class));
        verify(wishlistRepository, times(1)).delete(item);
    }
}

