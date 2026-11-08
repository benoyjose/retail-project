package com.retail.inventory.service;

import com.retail.common.event.OrderItemDto;
import com.retail.inventory.dto.InventoryResponse;
import com.retail.inventory.dto.StockAdjustRequest;
import com.retail.inventory.dto.StockReservationResult;
import com.retail.inventory.entity.InventoryItem;
import com.retail.inventory.repository.InventoryRepository;
import com.retail.inventory.service.impl.InventoryServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(inventoryService, "stockReservedTopic", "test-stock-reserved-topic");
        ReflectionTestUtils.setField(inventoryService, "stockReservationFailedTopic", "test-stock-reservation-failed-topic");

        testItem = InventoryItem.builder()
                .id(1L)
                .productSku("SKU-LAPTOP-01")
                .stockQuantity(50)
                .reservedQuantity(5)
                .lowStockThreshold(10)
                .build();
    }

    @Test
    @DisplayName("Should successfully reserve stock and publish StockReservedEvent")
    void shouldReserveStockSuccessfully() {
        OrderItemDto itemDto = OrderItemDto.builder()
                .productSku("SKU-LAPTOP-01")
                .quantity(3)
                .unitPrice(new BigDecimal("1000.00"))
                .build();

        when(inventoryRepository.findByProductSku("SKU-LAPTOP-01")).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        StockReservationResult result = inventoryService.reserveStock("ORD-1001", List.of(itemDto));

        assertTrue(result.isSuccess());
        assertEquals("ORD-1001", result.getOrderNumber());
        assertEquals(8, testItem.getReservedQuantity()); // 5 + 3 = 8

        verify(kafkaTemplate, times(1)).send(eq("test-stock-reserved-topic"), eq("ORD-1001"), any());
    }

    @Test
    @DisplayName("Should fail reservation and publish StockReservationFailedEvent when insufficient stock")
    void shouldFailReservationWhenStockInsufficient() {
        OrderItemDto itemDto = OrderItemDto.builder()
                .productSku("SKU-LAPTOP-01")
                .quantity(100) // Available is only 45
                .unitPrice(new BigDecimal("1000.00"))
                .build();

        when(inventoryRepository.findByProductSku("SKU-LAPTOP-01")).thenReturn(Optional.of(testItem));

        StockReservationResult result = inventoryService.reserveStock("ORD-1002", List.of(itemDto));

        assertFalse(result.isSuccess());
        assertTrue(result.getFailureReason().contains("Insufficient stock"));
        assertEquals(5, testItem.getReservedQuantity()); // Unchanged

        verify(kafkaTemplate, times(1)).send(eq("test-stock-reservation-failed-topic"), eq("ORD-1002"), any());
    }

    @Test
    @DisplayName("Should adjust stock level and lowStockThreshold")
    void shouldAdjustStock() {
        StockAdjustRequest request = StockAdjustRequest.builder()
                .quantityAdjustment(25)
                .newLowStockThreshold(15)
                .build();

        when(inventoryRepository.findByProductSku("SKU-LAPTOP-01")).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(InventoryItem.class))).thenReturn(testItem);

        InventoryResponse response = inventoryService.adjustStock("SKU-LAPTOP-01", request);

        assertNotNull(response);
        assertEquals(75, testItem.getStockQuantity()); // 50 + 25 = 75
        assertEquals(15, testItem.getLowStockThreshold());
    }
}

