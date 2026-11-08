package com.retail.inventory.dto;

import com.retail.common.event.OrderItemDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    @NotEmpty(message = "Items list cannot be empty")
    private List<OrderItemDto> items;
}

