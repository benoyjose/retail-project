package com.retail.order.controller;

import com.retail.common.dto.ApiResponse;
import com.retail.order.dto.OrderCheckoutRequest;
import com.retail.order.dto.OrderResponse;
import com.retail.order.dto.OrderStatusUpdateRequest;
import com.retail.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order Placement & Tracking", description = "Checkout Saga Orchestration and Tracking APIs (Use Cases 4 & 5)")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Place order and initiate distributed checkout Saga (Use Case 4)")
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @Valid @RequestBody OrderCheckoutRequest request) {
        Long customerId = Long.parseLong(userId);
        OrderResponse response = orderService.checkoutOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Order placed successfully. Saga orchestration in progress."));
    }

    @Operation(summary = "Get authenticated customer's order history (Use Case 5)")
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") String userId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Long customerId = Long.parseLong(userId);
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @Operation(summary = "Track real-time order status and shipment milestone (Use Case 5)")
    @GetMapping("/{orderNumber}/track")
    public ResponseEntity<ApiResponse<OrderResponse>> trackOrder(
            @PathVariable("orderNumber") String orderNumber) {
        OrderResponse response = orderService.trackOrder(orderNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @Operation(summary = "Update order status (Store Manager)")
    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable("orderNumber") String orderNumber,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateOrderStatus(orderNumber, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Order status updated successfully"));
    }

    @Operation(summary = "Internal API to verify if customer has purchased a SKU (for reviews)")
    @GetMapping("/verify-purchase")
    public ResponseEntity<ApiResponse<Boolean>> verifyPurchase(
            @RequestParam("customerId") Long customerId,
            @RequestParam("productSku") String productSku) {
        boolean purchased = orderService.hasCustomerPurchased(customerId, productSku);
        return ResponseEntity.ok(ApiResponse.ok(purchased));
    }
}

