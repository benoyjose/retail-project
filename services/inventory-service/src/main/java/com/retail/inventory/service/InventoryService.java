package com.retail.inventory.service;

import com.retail.common.event.OrderItemDto;
import com.retail.inventory.dto.InventoryResponse;
import com.retail.inventory.dto.StockAdjustRequest;
import com.retail.inventory.dto.StockReservationResult;

import java.util.List;

public interface InventoryService {
    InventoryResponse getInventoryBySku(String sku);
    InventoryResponse adjustStock(String sku, StockAdjustRequest request);
    StockReservationResult reserveStock(String orderNumber, List<OrderItemDto> items);
    void releaseStock(String orderNumber, List<OrderItemDto> items);
    void confirmStockDeduction(String orderNumber, List<OrderItemDto> items);
}

