package com.retail.order.repository;

import com.retail.common.enums.OrderStatus;
import com.retail.order.entity.Order;
import com.retail.order.entity.OrderLineItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Should persist and find order with encrypted shipping address by orderNumber")
    void shouldPersistAndFindOrderByNumber() {
        Order order = Order.builder()
                .orderNumber("ORD-202611-001")
                .customerId(10L)
                .totalAmount(new BigDecimal("1299.99"))
                .orderStatus(OrderStatus.CREATED)
                .shippingAddress("123 Retail Lane, Tech City, CA 94016")
                .trackingNumber("TRK-100200300")
                .build();

        OrderLineItem item = OrderLineItem.builder()
                .productSku("SKU-LAPTOP-01")
                .productName("Pro Laptop 15")
                .quantity(1)
                .unitPrice(new BigDecimal("1299.99"))
                .subtotal(new BigDecimal("1299.99"))
                .build();

        order.addItem(item);
        orderRepository.save(order);

        Optional<Order> found = orderRepository.findByOrderNumber("ORD-202611-001");
        assertTrue(found.isPresent());
        assertEquals("ORD-202611-001", found.get().getOrderNumber());
        assertEquals(OrderStatus.CREATED, found.get().getOrderStatus());
        assertEquals("123 Retail Lane, Tech City, CA 94016", found.get().getShippingAddress());
        assertEquals(1, found.get().getItems().size());
        assertEquals("SKU-LAPTOP-01", found.get().getItems().get(0).getProductSku());
    }

    @Test
    @DisplayName("Should verify existsByCustomerIdAndItems_ProductSku correctly")
    void shouldVerifyCustomerPurchase() {
        Order order = Order.builder()
                .orderNumber("ORD-202611-002")
                .customerId(25L)
                .totalAmount(new BigDecimal("50.00"))
                .orderStatus(OrderStatus.CONFIRMED)
                .shippingAddress("456 Market St")
                .build();

        OrderLineItem item = OrderLineItem.builder()
                .productSku("SKU-MOUSE-01")
                .productName("Ergonomic Mouse")
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("50.00"))
                .build();

        order.addItem(item);
        orderRepository.save(order);

        assertTrue(orderRepository.existsByCustomerIdAndItems_ProductSku(25L, "SKU-MOUSE-01"));
        assertFalse(orderRepository.existsByCustomerIdAndItems_ProductSku(25L, "SKU-KEYBOARD-01"));
    }
}

