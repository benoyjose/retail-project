package com.retail.order.service;

import com.retail.common.dto.ApiResponse;
import com.retail.common.enums.OrderStatus;
import com.retail.common.enums.PaymentStatus;
import com.retail.common.event.PaymentProcessedEvent;
import com.retail.common.event.StockReservedEvent;
import com.retail.order.dto.*;
import com.retail.order.entity.Order;
import com.retail.order.entity.OrderLineItem;
import com.retail.order.feign.CartFeignClient;
import com.retail.order.repository.OrderRepository;
import com.retail.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartFeignClient cartFeignClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "orderCreatedTopic", "test-order-created-topic");
        ReflectionTestUtils.setField(orderService, "processPaymentTopic", "test-process-payment-topic");
        ReflectionTestUtils.setField(orderService, "orderConfirmedTopic", "test-order-confirmed-topic");
        ReflectionTestUtils.setField(orderService, "releaseStockTopic", "test-release-stock-topic");

        testOrder = Order.builder()
                .id(1L)
                .orderNumber("ORD-1001")
                .customerId(10L)
                .totalAmount(new BigDecimal("1080.00"))
                .orderStatus(OrderStatus.CREATED)
                .shippingAddress("123 Test Street")
                .items(new ArrayList<>())
                .build();

        OrderLineItem item = OrderLineItem.builder()
                .productSku("SKU-LAPTOP-01")
                .productName("Pro Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("1000.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();
        testOrder.addItem(item);
    }

    @Test
    @DisplayName("Should successfully place order and publish OrderCreatedEvent to initiate Saga")
    void shouldCheckoutOrderSuccessfully() {
        CartItemDto cartItem = CartItemDto.builder()
                .productSku("SKU-LAPTOP-01")
                .productName("Pro Laptop")
                .quantity(1)
                .unitPrice(new BigDecimal("1000.00"))
                .subtotal(new BigDecimal("1000.00"))
                .build();

        CartResponseDto cartResponse = CartResponseDto.builder()
                .customerId(10L)
                .items(List.of(cartItem))
                .grandTotal(new BigDecimal("1080.00"))
                .build();

        when(cartFeignClient.getCart("10")).thenReturn(ApiResponse.ok(cartResponse));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        OrderCheckoutRequest checkoutRequest = OrderCheckoutRequest.builder()
                .shippingAddress("123 Test Street")
                .paymentMethod("CREDIT_CARD")
                .build();

        OrderResponse response = orderService.checkoutOrder(10L, checkoutRequest);

        assertNotNull(response);
        assertEquals(OrderStatus.CREATED, response.getOrderStatus());
        assertEquals(new BigDecimal("1080.00"), response.getTotalAmount());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(eq("test-order-created-topic"), eq("ORD-1001"), any());
    }

    @Test
    @DisplayName("Saga Step: Stock reserved should update order to STOCK_RESERVED and trigger payment")
    void shouldHandleStockReserved() {
        StockReservedEvent event = StockReservedEvent.builder()
                .orderNumber("ORD-1001")
                .reservedAt(Instant.now())
                .build();

        when(orderRepository.findByOrderNumber("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.handleStockReserved(event);

        assertEquals(OrderStatus.STOCK_RESERVED, testOrder.getOrderStatus());
        verify(kafkaTemplate, times(1)).send(eq("test-process-payment-topic"), eq("ORD-1001"), any());
    }

    @Test
    @DisplayName("Saga Step: Payment processed SUCCESS should CONFIRM order and publish OrderConfirmedEvent")
    void shouldHandlePaymentSuccess() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .orderNumber("ORD-1001")
                .paymentReference("PAY-12345")
                .status(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        when(orderRepository.findByOrderNumber("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.handlePaymentProcessed(event);

        assertEquals(OrderStatus.CONFIRMED, testOrder.getOrderStatus());
        assertNotNull(testOrder.getTrackingNumber());
        verify(kafkaTemplate, times(1)).send(eq("test-order-confirmed-topic"), eq("ORD-1001"), any());
    }

    @Test
    @DisplayName("Saga Step: Payment processed FAILED should CANCEL order and emit compensating ReleaseStockCommand")
    void shouldHandlePaymentFailureAndCompensate() {
        PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                .orderNumber("ORD-1001")
                .paymentReference("PAY-FAILED")
                .status(PaymentStatus.FAILED)
                .failureReason("Declined")
                .processedAt(Instant.now())
                .build();

        when(orderRepository.findByOrderNumber("ORD-1001")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        orderService.handlePaymentProcessed(event);

        assertEquals(OrderStatus.CANCELLED_PAYMENT_FAILED, testOrder.getOrderStatus());
        verify(kafkaTemplate, times(1)).send(eq("test-release-stock-topic"), eq("ORD-1001"), any());
    }
}

