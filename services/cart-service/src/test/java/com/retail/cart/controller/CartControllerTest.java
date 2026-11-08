package com.retail.cart.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.cart.dto.CartItemRequest;
import com.retail.cart.dto.CartItemResponse;
import com.retail.cart.dto.CartResponse;
import com.retail.cart.service.CartService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = false)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("GET /api/v1/cart - Success 200 OK")
    void shouldGetCartSuccessfully() throws Exception {
        CartItemResponse item = CartItemResponse.builder()
                .id(1L)
                .productSku("SKU-LAPTOP-01")
                .productName("Pro Laptop")
                .unitPrice(new BigDecimal("1000.00"))
                .quantity(1)
                .subtotal(new BigDecimal("1000.00"))
                .build();

        CartResponse response = CartResponse.builder()
                .customerId(10L)
                .items(List.of(item))
                .totalItems(1)
                .subtotal(new BigDecimal("1000.00"))
                .estimatedTax(new BigDecimal("80.00"))
                .grandTotal(new BigDecimal("1080.00"))
                .build();

        when(cartService.getCart(10L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/cart")
                        .header("X-User-Id", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.customerId").value(10))
                .andExpect(jsonPath("$.data.grandTotal").value(1080.00));
    }

    @Test
    @DisplayName("POST /api/v1/cart/items - Success 200 OK")
    void shouldAddItemToCartSuccessfully() throws Exception {
        CartItemRequest request = CartItemRequest.builder()
                .productSku("SKU-MOUSE-01")
                .quantity(2)
                .build();

        CartResponse response = CartResponse.builder()
                .customerId(10L)
                .items(List.of())
                .totalItems(2)
                .subtotal(new BigDecimal("100.00"))
                .estimatedTax(new BigDecimal("8.00"))
                .grandTotal(new BigDecimal("108.00"))
                .build();

        when(cartService.addItem(eq(10L), any(CartItemRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cart/items")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(2));
    }

    @Test
    @DisplayName("DELETE /api/v1/cart/items/{sku} - Success 200 OK")
    void shouldRemoveItemFromCart() throws Exception {
        CartResponse response = CartResponse.builder()
                .customerId(10L)
                .items(List.of())
                .totalItems(0)
                .subtotal(BigDecimal.ZERO)
                .estimatedTax(BigDecimal.ZERO)
                .grandTotal(BigDecimal.ZERO)
                .build();

        when(cartService.removeItem(10L, "SKU-MOUSE-01")).thenReturn(response);

        mockMvc.perform(delete("/api/v1/cart/items/SKU-MOUSE-01")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalItems").value(0));
    }
}

