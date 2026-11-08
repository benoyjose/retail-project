package com.retail.cart.repository;

import com.retail.cart.entity.CartItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("Should persist and find cart items by customerId")
    void shouldPersistAndFindCartItems() {
        CartItem item1 = CartItem.builder()
                .customerId(10L)
                .productSku("SKU-LAPTOP-01")
                .productName("Pro Laptop")
                .unitPrice(new BigDecimal("1200.00"))
                .quantity(1)
                .build();
        item1.calculateSubtotal();

        CartItem item2 = CartItem.builder()
                .customerId(10L)
                .productSku("SKU-MOUSE-01")
                .productName("Wireless Mouse")
                .unitPrice(new BigDecimal("50.00"))
                .quantity(2)
                .build();
        item2.calculateSubtotal();

        cartItemRepository.save(item1);
        cartItemRepository.save(item2);

        List<CartItem> items = cartItemRepository.findByCustomerId(10L);
        assertEquals(2, items.size());

        Optional<CartItem> laptop = cartItemRepository.findByCustomerIdAndProductSku(10L, "SKU-LAPTOP-01");
        assertTrue(laptop.isPresent());
        assertEquals(new BigDecimal("1200.00"), laptop.get().getSubtotal());
    }

    @Test
    @DisplayName("Should delete all items by customerId")
    void shouldDeleteAllItemsByCustomerId() {
        CartItem item = CartItem.builder()
                .customerId(20L)
                .productSku("SKU-KEYBOARD-01")
                .productName("Mechanical Keyboard")
                .unitPrice(new BigDecimal("80.00"))
                .quantity(1)
                .build();
        item.calculateSubtotal();

        cartItemRepository.save(item);
        assertEquals(1, cartItemRepository.findByCustomerId(20L).size());

        cartItemRepository.deleteByCustomerId(20L);
        assertEquals(0, cartItemRepository.findByCustomerId(20L).size());
    }
}

