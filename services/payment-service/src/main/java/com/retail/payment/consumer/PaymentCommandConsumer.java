package com.retail.payment.consumer;

import com.retail.common.event.ProcessPaymentCommand;
import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "${app.kafka.topics.process-payment:process-payment-topic}", groupId = "payment-service-group")
    public void handleProcessPaymentCommand(ProcessPaymentCommand command) {
        log.info("Payment consumer received ProcessPaymentCommand for order: {}, amount: {}",
                command.getOrderNumber(), command.getAmount());

        PaymentProcessRequest request = PaymentProcessRequest.builder()
                .orderNumber(command.getOrderNumber())
                .customerId(command.getCustomerId())
                .amount(command.getAmount())
                .paymentMethod(command.getPaymentMethod() != null ? command.getPaymentMethod() : "CREDIT_CARD")
                .build();

        try {
            paymentService.processPayment(request);
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", command.getOrderNumber(), e);
        }
    }
}

