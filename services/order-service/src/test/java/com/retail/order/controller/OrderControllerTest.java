package com.retail.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.common.enums.OrderStatus;
import com.retail.order.dto.OrderCheckoutRequest;
import com.retail.order.dto.OrderResponse;
import com.retail.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("POST /api/v1/orders/checkout - Success 201 Created")
    void shouldCheckoutOrderSuccessfully() throws Exception {
        OrderCheckoutRequest request = OrderCheckoutRequest.builder()
                .shippingAddress("123 Tech Boulevard, Suite 100")
                .paymentMethod("CREDIT_CARD")
                .build();

        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-1001")
                .customerId(10L)
                .totalAmount(new BigDecimal("1080.00"))
                .orderStatus(OrderStatus.CREATED)
                .shippingAddress("123 Tech Boulevard, Suite 100")
                .items(List.of())
                .build();

        when(orderService.checkoutOrder(eq(10L), any(OrderCheckoutRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-User-Id", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.data.orderStatus").value("CREATED"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/{orderNumber}/track - Success 200 OK")
    void shouldTrackOrder() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-1001")
                .customerId(10L)
                .totalAmount(new BigDecimal("1080.00"))
                .orderStatus(OrderStatus.CONFIRMED)
                .trackingNumber("TRK-FEDEX-12345")
                .items(List.of())
                .build();

        when(orderService.trackOrder("ORD-1001")).thenReturn(response);

        mockMvc.perform(get("/api/v1/orders/ORD-1001/track"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderNumber").value("ORD-1001"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK-FEDEX-12345"));
    }

    @Test
    @DisplayName("GET /api/v1/orders/my-orders - Success 200 OK")
    void shouldGetMyOrders() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .orderNumber("ORD-1001")
                .totalAmount(new BigDecimal("1080.00"))
                .orderStatus(OrderStatus.CONFIRMED)
                .items(List.of())
                .build();

        when(orderService.getCustomerOrders(eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response)));

        mockMvc.perform(get("/api/v1/orders/my-orders")
                        .header("X-User-Id", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].orderNumber").value("ORD-1001"));
    }
}

