package com.retail.inventory.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.inventory.dto.InventoryResponse;
import com.retail.inventory.dto.StockAdjustRequest;
import com.retail.inventory.dto.StockReservationRequest;
import com.retail.inventory.dto.StockReservationResult;
import com.retail.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory & Stock Control", description = "Store Manager Inventory and Stock Reservation APIs (Use Case 7 & 4)")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "Get current stock and threshold for a SKU")
    @GetMapping("/{sku}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryBySku(@PathVariable("sku") String sku) {
        InventoryResponse response = inventoryService.getInventoryBySku(sku);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Adjust inventory stock levels (Store Manager Use Case 7)")
    @PutMapping("/{sku}/adjust")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @PathVariable("sku") String sku,
            @Valid @RequestBody StockAdjustRequest request) {
        InventoryResponse response = inventoryService.adjustStock(sku, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Stock levels updated successfully"));
    }

    @Operation(summary = "Internal API for stock reservation during checkout (Use Case 4)")
    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<StockReservationResult>> reserveStock(
            @Valid @RequestBody StockReservationRequest request) {
        StockReservationResult result = inventoryService.reserveStock(request.getOrderNumber(), request.getItems());
        return ResponseEntity.ok(ApiResponse.ok(result, result.isSuccess() ? "Stock reserved" : "Stock reservation failed"));
    }
}

