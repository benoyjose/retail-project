package com.retail.order.service;

import com.retail.common.event.PaymentProcessedEvent;
import com.retail.common.event.StockReservationFailedEvent;
import com.retail.common.event.StockReservedEvent;
import com.retail.order.dto.OrderCheckoutRequest;
import com.retail.order.dto.OrderResponse;
import com.retail.order.dto.OrderStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse checkoutOrder(Long customerId, OrderCheckoutRequest request);
    Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable);
    OrderResponse trackOrder(String orderNumber);
    OrderResponse updateOrderStatus(String orderNumber, OrderStatusUpdateRequest request);
    boolean hasCustomerPurchased(Long customerId, String productSku);

    // Saga Event Handlers
    void handleStockReserved(StockReservedEvent event);
    void handleStockReservationFailed(StockReservationFailedEvent event);
    void handlePaymentProcessed(PaymentProcessedEvent event);
}

