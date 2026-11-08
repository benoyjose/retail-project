package com.retail.inventory.service.impl;

import com.retail.common.event.OrderItemDto;
import com.retail.common.event.StockReservationFailedEvent;
import com.retail.common.event.StockReservedEvent;
import com.retail.inventory.dto.InventoryResponse;
import com.retail.inventory.dto.StockAdjustRequest;
import com.retail.inventory.dto.StockReservationResult;
import com.retail.inventory.entity.InventoryItem;
import com.retail.inventory.exception.InsufficientStockException;
import com.retail.inventory.exception.InventoryNotFoundException;
import com.retail.inventory.repository.InventoryRepository;
import com.retail.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.stock-reserved:stock-reserved-topic}")
    private String stockReservedTopic;

    @Value("${app.kafka.topics.stock-reservation-failed:stock-reservation-failed-topic}")
    private String stockReservationFailedTopic;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryBySku(String sku) {
        String cleanSku = sku.trim().toUpperCase();
        InventoryItem item = inventoryRepository.findByProductSku(cleanSku)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for SKU: " + cleanSku));
        return mapToResponse(item);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(String sku, StockAdjustRequest request) {
        String cleanSku = sku.trim().toUpperCase();
        log.info("Manager adjusting stock for SKU {}: adjustment={}", cleanSku, request.getQuantityAdjustment());

        InventoryItem item = inventoryRepository.findByProductSku(cleanSku)
                .orElseGet(() -> InventoryItem.builder()
                        .productSku(cleanSku)
                        .stockQuantity(0)
                        .reservedQuantity(0)
                        .lowStockThreshold(10)
                        .build());

        int newStock = item.getStockQuantity() + request.getQuantityAdjustment();
        if (newStock < 0) {
            throw new InsufficientStockException("Cannot reduce stock below zero. Current stock: " + item.getStockQuantity());
        }

        item.setStockQuantity(newStock);
        if (request.getNewLowStockThreshold() != null && request.getNewLowStockThreshold() >= 0) {
            item.setLowStockThreshold(request.getNewLowStockThreshold());
        }

        InventoryItem saved = inventoryRepository.save(item);
        log.info("Stock updated for SKU {}: new stock={}", cleanSku, saved.getStockQuantity());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public StockReservationResult reserveStock(String orderNumber, List<OrderItemDto> items) {
        log.info("Attempting to reserve stock for order {}: {} items", orderNumber, items.size());

        List<InventoryItem> itemsToReserve = new ArrayList<>();

        // Validation pass
        for (OrderItemDto orderItem : items) {
            String sku = orderItem.getProductSku().trim().toUpperCase();
            InventoryItem inventory = inventoryRepository.findByProductSku(sku).orElse(null);

            if (inventory == null || inventory.getAvailableQuantity() < orderItem.getQuantity()) {
                String reason = "Insufficient stock for SKU: " + sku +
                        (inventory != null ? " (Available: " + inventory.getAvailableQuantity() + ", Requested: " + orderItem.getQuantity() + ")" : " (Not found)");
                log.warn("Reservation failed for order {}: {}", orderNumber, reason);

                publishReservationFailed(orderNumber, reason);
                return StockReservationResult.builder()
                        .orderNumber(orderNumber)
                        .success(false)
                        .failureReason(reason)
                        .build();
            }
            itemsToReserve.add(inventory);
        }

        // Reservation pass
        for (int i = 0; i < items.size(); i++) {
            OrderItemDto orderItem = items.get(i);
            InventoryItem inventory = itemsToReserve.get(i);
            inventory.setReservedQuantity(inventory.getReservedQuantity() + orderItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        log.info("Stock successfully reserved for order {}", orderNumber);
        publishStockReserved(orderNumber, items);

        return StockReservationResult.builder()
                .orderNumber(orderNumber)
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public void releaseStock(String orderNumber, List<OrderItemDto> items) {
        log.info("Compensating Saga: Releasing reserved stock for cancelled order {}", orderNumber);
        if (items == null) return;

        for (OrderItemDto orderItem : items) {
            String sku = orderItem.getProductSku().trim().toUpperCase();
            inventoryRepository.findByProductSku(sku).ifPresent(inventory -> {
                int newReserved = Math.max(0, inventory.getReservedQuantity() - orderItem.getQuantity());
                inventory.setReservedQuantity(newReserved);
                inventoryRepository.save(inventory);
                log.info("Released {} reserved units for SKU {}", orderItem.getQuantity(), sku);
            });
        }
    }

    @Override
    @Transactional
    public void confirmStockDeduction(String orderNumber, List<OrderItemDto> items) {
        log.info("Confirming physical stock deduction for confirmed order {}", orderNumber);
        if (items == null) return;

        for (OrderItemDto orderItem : items) {
            String sku = orderItem.getProductSku().trim().toUpperCase();
            inventoryRepository.findByProductSku(sku).ifPresent(inventory -> {
                int newStock = Math.max(0, inventory.getStockQuantity() - orderItem.getQuantity());
                int newReserved = Math.max(0, inventory.getReservedQuantity() - orderItem.getQuantity());
                inventory.setStockQuantity(newStock);
                inventory.setReservedQuantity(newReserved);
                inventoryRepository.save(inventory);
                log.info("Deducted {} physical units for SKU {}", orderItem.getQuantity(), sku);
            });
        }
    }

    private void publishStockReserved(String orderNumber, List<OrderItemDto> items) {
        try {
            StockReservedEvent event = StockReservedEvent.builder()
                    .orderNumber(orderNumber)
                    .items(items)
                    .reservedAt(Instant.now())
                    .build();
            kafkaTemplate.send(stockReservedTopic, orderNumber, event);
            log.info("Published StockReservedEvent to topic '{}' for order: {}", stockReservedTopic, orderNumber);
        } catch (Exception e) {
            log.error("Failed to publish StockReservedEvent for order {}", orderNumber, e);
        }
    }

    private void publishReservationFailed(String orderNumber, String reason) {
        try {
            StockReservationFailedEvent event = StockReservationFailedEvent.builder()
                    .orderNumber(orderNumber)
                    .reason(reason)
                    .failedAt(Instant.now())
                    .build();
            kafkaTemplate.send(stockReservationFailedTopic, orderNumber, event);
            log.info("Published StockReservationFailedEvent to topic '{}' for order: {}", stockReservationFailedTopic, orderNumber);
        } catch (Exception e) {
            log.error("Failed to publish StockReservationFailedEvent for order {}", orderNumber, e);
        }
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        return InventoryResponse.builder()
                .id(item.getId())
                .productSku(item.getProductSku())
                .stockQuantity(item.getStockQuantity())
                .reservedQuantity(item.getReservedQuantity())
                .availableQuantity(item.getAvailableQuantity())
                .lowStockThreshold(item.getLowStockThreshold())
                .lowStock(item.isLowStock())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}

