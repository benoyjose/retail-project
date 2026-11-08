package com.retail.order.consumer;

import com.retail.common.event.PaymentProcessedEvent;
import com.retail.common.event.StockReservationFailedEvent;
import com.retail.common.event.StockReservedEvent;
import com.retail.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventsConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "${app.kafka.topics.stock-reserved:stock-reserved-topic}", groupId = "order-saga-group")
    public void onStockReserved(StockReservedEvent event) {
        log.info("Saga Consumer: Received StockReservedEvent for orderNumber: {}", event.getOrderNumber());
        orderService.handleStockReserved(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.stock-reservation-failed:stock-reservation-failed-topic}", groupId = "order-saga-group")
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        log.info("Saga Consumer: Received StockReservationFailedEvent for orderNumber: {}", event.getOrderNumber());
        orderService.handleStockReservationFailed(event);
    }

    @KafkaListener(topics = "${app.kafka.topics.payment-processed:payment-processed-topic}", groupId = "order-saga-group")
    public void onPaymentProcessed(PaymentProcessedEvent event) {
        log.info("Saga Consumer: Received PaymentProcessedEvent for orderNumber: {}, status: {}",
                event.getOrderNumber(), event.getStatus());
        orderService.handlePaymentProcessed(event);
    }
}

