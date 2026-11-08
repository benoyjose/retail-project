package com.retail.inventory.consumer;

import com.retail.common.event.OrderConfirmedEvent;
import com.retail.common.event.OrderCreatedEvent;
import com.retail.common.event.ReleaseStockCommand;
import com.retail.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventsConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "${app.kafka.topics.order-created:order-created-topic}", groupId = "inventory-service-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Inventory consumer received OrderCreatedEvent for order: {}", event.getOrderNumber());
        inventoryService.reserveStock(event.getOrderNumber(), event.getItems());
    }

    @KafkaListener(topics = "${app.kafka.topics.release-stock:release-stock-topic}", groupId = "inventory-service-group")
    public void handleReleaseStock(ReleaseStockCommand command) {
        log.info("Inventory consumer received ReleaseStockCommand for order: {}", command.getOrderNumber());
        inventoryService.releaseStock(command.getOrderNumber(), command.getItems());
    }

    @KafkaListener(topics = "${app.kafka.topics.order-confirmed:order-confirmed-topic}", groupId = "inventory-service-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Inventory consumer received OrderConfirmedEvent for order: {}", event.getOrderNumber());
        inventoryService.confirmStockDeduction(event.getOrderNumber(), event.getItems());
    }
}

