package com.retail.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleaseStockCommand {
    private String orderNumber;
    private List<OrderItemDto> items;
    private String reason;
    private Instant requestedAt;
}

