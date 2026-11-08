package com.retail.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.inventory.dto.InventoryResponse;
import com.retail.inventory.dto.StockAdjustRequest;
import com.retail.inventory.dto.StockReservationRequest;
import com.retail.inventory.dto.StockReservationResult;
import com.retail.inventory.service.InventoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    @DisplayName("GET /api/v1/inventory/{sku} - Success 200 OK")
    void shouldGetInventoryBySku() throws Exception {
        InventoryResponse response = InventoryResponse.builder()
                .id(1L)
                .productSku("SKU-LAPTOP-01")
                .stockQuantity(100)
                .reservedQuantity(10)
                .availableQuantity(90)
                .lowStockThreshold(15)
                .lowStock(false)
                .build();

        when(inventoryService.getInventoryBySku("SKU-LAPTOP-01")).thenReturn(response);

        mockMvc.perform(get("/api/v1/inventory/SKU-LAPTOP-01")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productSku").value("SKU-LAPTOP-01"))
                .andExpect(jsonPath("$.data.availableQuantity").value(90));
    }

    @Test
    @DisplayName("PUT /api/v1/inventory/{sku}/adjust - Success 200 OK")
    void shouldAdjustStock() throws Exception {
        StockAdjustRequest request = StockAdjustRequest.builder()
                .quantityAdjustment(50)
                .build();

        InventoryResponse response = InventoryResponse.builder()
                .id(1L)
                .productSku("SKU-LAPTOP-01")
                .stockQuantity(150)
                .reservedQuantity(10)
                .availableQuantity(140)
                .build();

        when(inventoryService.adjustStock(eq("SKU-LAPTOP-01"), any(StockAdjustRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/inventory/SKU-LAPTOP-01/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.stockQuantity").value(150));
    }

    @Test
    @DisplayName("POST /api/v1/inventory/reserve - Success 200 OK")
    void shouldReserveStock() throws Exception {
        com.retail.common.event.OrderItemDto item = com.retail.common.event.OrderItemDto.builder()
                .productSku("SKU-LAPTOP-01")
                .quantity(1)
                .build();

        StockReservationRequest request = StockReservationRequest.builder()
                .orderNumber("ORD-999")
                .items(List.of(item))
                .build();

        StockReservationResult result = StockReservationResult.builder()
                .orderNumber("ORD-999")
                .success(true)
                .build();

        when(inventoryService.reserveStock(eq("ORD-999"), any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/inventory/reserve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true));
    }
}
