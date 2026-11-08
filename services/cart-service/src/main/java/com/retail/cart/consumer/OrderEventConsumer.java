package com.retail.cart.consumer;

import com.retail.common.event.OrderConfirmedEvent;
import com.retail.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final CartService cartService;

    @KafkaListener(topics = "${app.kafka.topics.order-confirmed:order-confirmed-topic}", groupId = "cart-service-group")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for orderNumber: {} and customerId: {}. Clearing shopping cart.",
                event.getOrderNumber(), event.getCustomerId());
        try {
            cartService.clearCart(event.getCustomerId());
        } catch (Exception e) {
            log.error("Failed to clear cart for customerId: {}", event.getCustomerId(), e);
        }
    }
}

