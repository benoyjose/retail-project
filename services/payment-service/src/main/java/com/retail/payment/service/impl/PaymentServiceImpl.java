package com.retail.payment.service.impl;

import com.retail.common.enums.PaymentStatus;
import com.retail.common.event.PaymentProcessedEvent;
import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.dto.PaymentResponse;
import com.retail.payment.entity.PaymentTransaction;
import com.retail.payment.exception.PaymentNotFoundException;
import com.retail.payment.repository.PaymentTransactionRepository;
import com.retail.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.payment-processed:payment-processed-topic}")
    private String paymentProcessedTopic;

    // Simulation trigger: amount == 999999.00 triggers a simulated payment decline
    private static final BigDecimal SIMULATED_DECLINE_AMOUNT = new BigDecimal("999999.00");

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentProcessRequest request) {
        log.info("Processing payment for order {}: amount={}, method={}",
                request.getOrderNumber(), request.getAmount(), request.getPaymentMethod());

        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        PaymentStatus status = PaymentStatus.SUCCESS;
        String failureReason = null;

        // Payment gateway simulation logic
        if (SIMULATED_DECLINE_AMOUNT.compareTo(request.getAmount()) == 0) {
            status = PaymentStatus.FAILED;
            failureReason = "Card declined: Insufficient funds or invalid security code";
            log.warn("Payment simulation DECLINED for order: {}", request.getOrderNumber());
        }

        PaymentTransaction transaction = PaymentTransaction.builder()
                .paymentReference(paymentRef)
                .orderNumber(request.getOrderNumber())
                .customerId(request.getCustomerId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(status)
                .failureReason(failureReason)
                .processedAt(Instant.now())
                .build();

        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        log.info("Payment transaction recorded with reference: {}, status: {}", saved.getPaymentReference(), saved.getPaymentStatus());

        publishPaymentProcessedEvent(saved);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {
        PaymentTransaction tx = paymentTransactionRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for order number: " + orderNumber));
        return mapToResponse(tx);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByReference(String paymentReference) {
        PaymentTransaction tx = paymentTransactionRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for reference: " + paymentReference));
        return mapToResponse(tx);
    }

    private void publishPaymentProcessedEvent(PaymentTransaction tx) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .orderNumber(tx.getOrderNumber())
                    .paymentReference(tx.getPaymentReference())
                    .amount(tx.getAmount())
                    .status(tx.getPaymentStatus())
                    .failureReason(tx.getFailureReason())
                    .processedAt(tx.getProcessedAt())
                    .build();

            kafkaTemplate.send(paymentProcessedTopic, tx.getOrderNumber(), event);
            log.info("Published PaymentProcessedEvent to topic '{}' for order: {}", paymentProcessedTopic, tx.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to publish PaymentProcessedEvent for order {}", tx.getOrderNumber(), e);
        }
    }

    private PaymentResponse mapToResponse(PaymentTransaction tx) {
        return PaymentResponse.builder()
                .paymentReference(tx.getPaymentReference())
                .orderNumber(tx.getOrderNumber())
                .customerId(tx.getCustomerId())
                .amount(tx.getAmount())
                .paymentMethod(tx.getPaymentMethod())
                .status(tx.getPaymentStatus())
                .failureReason(tx.getFailureReason())
                .processedAt(tx.getProcessedAt())
                .build();
    }
}

