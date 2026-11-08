package com.retail.order.dto;

import com.retail.common.enums.OrderStatus;
import com.retail.common.event.OrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private String shippingAddress;
    private String trackingNumber;
    private List<OrderItemDto> items;
    private Instant createdAt;
    private Instant updatedAt;
}

