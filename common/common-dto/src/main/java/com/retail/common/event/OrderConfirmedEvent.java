package com.retail.common.event;

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
public class OrderConfirmedEvent {
    private String orderNumber;
    private Long customerId;
    private BigDecimal totalAmount;
    private List<OrderItemDto> items;
    private String trackingNumber;
    private Instant confirmedAt;
}

