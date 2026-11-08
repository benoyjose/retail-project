package com.retail.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustRequest {

    @NotNull(message = "Quantity adjustment is required (positive to add, negative to reduce)")
    private Integer quantityAdjustment;

    private Integer newLowStockThreshold;
}

