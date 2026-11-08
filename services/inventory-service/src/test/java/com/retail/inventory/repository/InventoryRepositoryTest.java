package com.retail.inventory.repository;

import com.retail.inventory.entity.InventoryItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class InventoryRepositoryTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    @DisplayName("Should persist and find inventory item by SKU")
    void shouldPersistAndFindInventoryBySku() {
        InventoryItem item = InventoryItem.builder()
                .productSku("SKU-LAPTOP-01")
                .stockQuantity(100)
                .reservedQuantity(10)
                .lowStockThreshold(15)
                .build();

        inventoryRepository.save(item);

        Optional<InventoryItem> found = inventoryRepository.findByProductSku("SKU-LAPTOP-01");
        assertTrue(found.isPresent());
        assertEquals(100, found.get().getStockQuantity());
        assertEquals(10, found.get().getReservedQuantity());
        assertEquals(90, found.get().getAvailableQuantity());
        assertFalse(found.get().isLowStock());
    }

    @Test
    @DisplayName("Should identify low stock condition")
    void shouldIdentifyLowStock() {
        InventoryItem item = InventoryItem.builder()
                .productSku("SKU-LOW-01")
                .stockQuantity(5)
                .reservedQuantity(0)
                .lowStockThreshold(10)
                .build();

        inventoryRepository.save(item);

        Optional<InventoryItem> found = inventoryRepository.findByProductSku("SKU-LOW-01");
        assertTrue(found.isPresent());
        assertTrue(found.get().isLowStock());
    }
}

