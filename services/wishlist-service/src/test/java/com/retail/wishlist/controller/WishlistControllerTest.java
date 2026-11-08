package com.retail.wishlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.wishlist.dto.WishlistItemRequest;
import com.retail.wishlist.dto.WishlistItemResponse;
import com.retail.wishlist.dto.WishlistResponse;
import com.retail.wishlist.service.WishlistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WishlistController.class)
@ActiveProfiles("test")
class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    @Test
    @DisplayName("GET /api/v1/wishlist - Should return customer wishlist")
    void shouldGetWishlist() throws Exception {
        WishlistItemResponse itemResponse = WishlistItemResponse.builder()
                .id(1L)
                .customerId(100L)
                .productSku("SKU-EARBUDS-01")
                .productName("Wireless Earbuds")
                .unitPrice(BigDecimal.valueOf(79.99))
                .addedAt(Instant.now())
                .build();

        WishlistResponse response = WishlistResponse.builder()
                .customerId(100L)
                .items(List.of(itemResponse))
                .totalItems(1)
                .build();

        when(wishlistService.getWishlist(100L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/wishlist")
                        .header("X-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(100L))
                .andExpect(jsonPath("$.data.items[0].productSku").value("SKU-EARBUDS-01"));
    }

    @Test
    @DisplayName("POST /api/v1/wishlist/items - Should add item to wishlist")
    void shouldAddItem() throws Exception {
        WishlistItemRequest request = new WishlistItemRequest("SKU-EARBUDS-01");
        WishlistItemResponse response = WishlistItemResponse.builder()
                .id(1L)
                .customerId(100L)
                .productSku("SKU-EARBUDS-01")
                .unitPrice(BigDecimal.valueOf(79.99))
                .build();

        when(wishlistService.addItem(eq(100L), any(WishlistItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/wishlist/items")
                        .header("X-User-Id", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productSku").value("SKU-EARBUDS-01"));
    }

    @Test
    @DisplayName("POST /api/v1/wishlist/items/{sku}/move-to-cart - Should move item to cart")
    void shouldMoveToCart() throws Exception {
        doNothing().when(wishlistService).moveToCart(100L, "SKU-EARBUDS-01");

        mockMvc.perform(post("/api/v1/wishlist/items/SKU-EARBUDS-01/move-to-cart")
                        .header("X-User-Id", 100L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Item moved to cart successfully"));
    }
}

