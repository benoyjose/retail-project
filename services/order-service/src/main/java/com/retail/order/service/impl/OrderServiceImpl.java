package com.retail.order.service.impl;

import com.retail.common.dto.ApiResponse;
import com.retail.common.enums.OrderStatus;
import com.retail.common.enums.PaymentStatus;
import com.retail.common.event.*;
import com.retail.order.dto.*;
import com.retail.order.entity.Order;
import com.retail.order.entity.OrderLineItem;
import com.retail.order.exception.EmptyCartException;
import com.retail.order.exception.OrderNotFoundException;
import com.retail.order.feign.CartFeignClient;
import com.retail.order.repository.OrderRepository;
import com.retail.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartFeignClient cartFeignClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.order-created:order-created-topic}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topics.process-payment:process-payment-topic}")
    private String processPaymentTopic;

    @Value("${app.kafka.topics.order-confirmed:order-confirmed-topic}")
    private String orderConfirmedTopic;

    @Value("${app.kafka.topics.release-stock:release-stock-topic}")
    private String releaseStockTopic;

    @Override
    @Transactional
    public OrderResponse checkoutOrder(Long customerId, OrderCheckoutRequest request) {
        log.info("Initiating checkout for customerId: {}", customerId);

        // 1. Retrieve cart items from cart-service
        CartResponseDto cart;
        try {
            ApiResponse<CartResponseDto> cartResponse = cartFeignClient.getCart(String.valueOf(customerId));
            if (cartResponse == null || cartResponse.getData() == null || cartResponse.getData().getItems().isEmpty()) {
                throw new EmptyCartException("Cannot place order. Shopping cart is empty.");
            }
            cart = cartResponse.getData();
        } catch (EmptyCartException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Feign call to cart-service failed, checking fallback items: {}", e.getMessage());
            throw new EmptyCartException("Shopping cart is unavailable or empty. Please try again.");
        }

        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(customerId)
                .totalAmount(cart.getGrandTotal())
                .orderStatus(OrderStatus.CREATED)
                .shippingAddress(request.getShippingAddress())
                .items(new ArrayList<>())
                .build();

        for (CartItemDto cartItem : cart.getItems()) {
            OrderLineItem item = OrderLineItem.builder()
                    .productSku(cartItem.getProductSku())
                    .productName(cartItem.getProductName())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .subtotal(cartItem.getSubtotal())
                    .build();
            order.addItem(item);
        }

        Order saved = orderRepository.save(order);
        log.info("Order created in database with orderNumber: {}, status: CREATED", saved.getOrderNumber());

        // 2. Publish OrderCreatedEvent to Kafka to trigger inventory reservation (Saga Step 1)
        publishOrderCreatedEvent(saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Saga Step: Stock reserved for orderNumber: {}. Transitioning order to STOCK_RESERVED.", event.getOrderNumber());
        Order order = orderRepository.findByOrderNumber(event.getOrderNumber()).orElse(null);
        if (order == null) {
            log.error("Order not found for orderNumber: {}", event.getOrderNumber());
            return;
        }

        order.setOrderStatus(OrderStatus.STOCK_RESERVED);
        orderRepository.save(order);

        // Emit ProcessPaymentCommand to trigger payment-service (Saga Step 2)
        publishProcessPaymentCommand(order);
    }

    @Override
    @Transactional
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        log.warn("Saga Step: Stock reservation failed for orderNumber: {}. Reason: {}. Transitioning order to CANCELLED_OUT_OF_STOCK.",
                event.getOrderNumber(), event.getReason());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber()).orElse(null);
        if (order != null) {
            order.setOrderStatus(OrderStatus.CANCELLED_OUT_OF_STOCK);
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Saga Step: Received PaymentProcessedEvent for orderNumber: {}, status: {}",
                event.getOrderNumber(), event.getStatus());

        Order order = orderRepository.findByOrderNumber(event.getOrderNumber()).orElse(null);
        if (order == null) return;

        if (event.getStatus() == PaymentStatus.SUCCESS) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
            order.setTrackingNumber(trackingNumber);
            orderRepository.save(order);
            log.info("Order {} CONFIRMED with tracking number {}", order.getOrderNumber(), trackingNumber);

            // Publish OrderConfirmedEvent to clear cart and deduct warehouse physical stock
            publishOrderConfirmedEvent(order);
        } else {
            order.setOrderStatus(OrderStatus.CANCELLED_PAYMENT_FAILED);
            orderRepository.save(order);
            log.warn("Order {} CANCELLED due to payment failure. Emitting compensating ReleaseStockCommand.", order.getOrderNumber());

            // Compensating transaction: Release reserved stock
            publishReleaseStockCommand(order, event.getFailureReason());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getCustomerOrders(Long customerId, Pageable pageable) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrder(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));
        return mapToResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderNumber, OrderStatusUpdateRequest request) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with number: " + orderNumber));

        order.setOrderStatus(request.getStatus());
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            order.setTrackingNumber(request.getTrackingNumber());
        }

        Order updated = orderRepository.save(order);
        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasCustomerPurchased(Long customerId, String productSku) {
        return orderRepository.existsByCustomerIdAndItems_ProductSku(customerId, productSku);
    }

    private void publishOrderCreatedEvent(Order order) {
        try {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(i -> OrderItemDto.builder()
                            .productSku(i.getProductSku())
                            .productName(i.getProductName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getSubtotal())
                            .build())
                    .collect(Collectors.toList());

            OrderCreatedEvent event = OrderCreatedEvent.builder()
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomerId())
                    .totalAmount(order.getTotalAmount())
                    .items(itemDtos)
                    .shippingAddress(order.getShippingAddress())
                    .createdAt(Instant.now())
                    .build();

            kafkaTemplate.send(orderCreatedTopic, order.getOrderNumber(), event);
            log.info("Published OrderCreatedEvent to topic '{}' for orderNumber: {}", orderCreatedTopic, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish OrderCreatedEvent for order: {}", order.getOrderNumber(), e);
        }
    }

    private void publishProcessPaymentCommand(Order order) {
        try {
            ProcessPaymentCommand command = ProcessPaymentCommand.builder()
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomerId())
                    .amount(order.getTotalAmount())
                    .paymentMethod("CREDIT_CARD")
                    .requestedAt(Instant.now())
                    .build();

            kafkaTemplate.send(processPaymentTopic, order.getOrderNumber(), command);
            log.info("Published ProcessPaymentCommand to topic '{}' for orderNumber: {}", processPaymentTopic, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish ProcessPaymentCommand for order: {}", order.getOrderNumber(), e);
        }
    }

    private void publishOrderConfirmedEvent(Order order) {
        try {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(i -> OrderItemDto.builder()
                            .productSku(i.getProductSku())
                            .productName(i.getProductName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getSubtotal())
                            .build())
                    .collect(Collectors.toList());

            OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                    .orderNumber(order.getOrderNumber())
                    .customerId(order.getCustomerId())
                    .totalAmount(order.getTotalAmount())
                    .items(itemDtos)
                    .trackingNumber(order.getTrackingNumber())
                    .confirmedAt(Instant.now())
                    .build();

            kafkaTemplate.send(orderConfirmedTopic, order.getOrderNumber(), event);
            log.info("Published OrderConfirmedEvent to topic '{}' for order: {}", orderConfirmedTopic, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish OrderConfirmedEvent for order: {}", order.getOrderNumber(), e);
        }
    }

    private void publishReleaseStockCommand(Order order, String reason) {
        try {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(i -> OrderItemDto.builder()
                            .productSku(i.getProductSku())
                            .productName(i.getProductName())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getSubtotal())
                            .build())
                    .collect(Collectors.toList());

            ReleaseStockCommand command = ReleaseStockCommand.builder()
                    .orderNumber(order.getOrderNumber())
                    .items(itemDtos)
                    .reason(reason)
                    .requestedAt(Instant.now())
                    .build();

            kafkaTemplate.send(releaseStockTopic, order.getOrderNumber(), command);
            log.info("Published ReleaseStockCommand to topic '{}' for order: {}", releaseStockTopic, order.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish ReleaseStockCommand for order: {}", order.getOrderNumber(), e);
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> OrderItemDto.builder()
                        .productSku(i.getProductSku())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .orderStatus(order.getOrderStatus())
                .shippingAddress(order.getShippingAddress())
                .trackingNumber(order.getTrackingNumber())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}

