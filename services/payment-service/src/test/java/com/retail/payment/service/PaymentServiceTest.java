package com.retail.payment.service;

import com.retail.common.enums.PaymentStatus;
import com.retail.payment.dto.PaymentProcessRequest;
import com.retail.payment.dto.PaymentResponse;
import com.retail.payment.entity.PaymentTransaction;
import com.retail.payment.repository.PaymentTransactionRepository;
import com.retail.payment.service.impl.PaymentServiceImpl;
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
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentTransactionRepository repository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "paymentProcessedTopic", "test-payment-processed-topic");
    }

    @Test
    @DisplayName("Should successfully process payment and emit PaymentProcessedEvent")
    void shouldProcessPaymentSuccessfully() {
        PaymentProcessRequest request = PaymentProcessRequest.builder()
                .orderNumber("ORD-1001")
                .customerId(10L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentTransaction savedTx = PaymentTransaction.builder()
                .id(1L)
                .paymentReference("PAY-1234567890AB")
                .orderNumber("ORD-1001")
                .customerId(10L)
                .amount(new BigDecimal("150.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentStatus(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        when(repository.save(any(PaymentTransaction.class))).thenReturn(savedTx);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals("ORD-1001", response.getOrderNumber());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertNull(response.getFailureReason());

        verify(repository, times(1)).save(any(PaymentTransaction.class));
        verify(kafkaTemplate, times(1)).send(eq("test-payment-processed-topic"), eq("ORD-1001"), any());
    }

    @Test
    @DisplayName("Should simulate payment decline when simulated amount matches")
    void shouldSimulatePaymentDecline() {
        PaymentProcessRequest request = PaymentProcessRequest.builder()
                .orderNumber("ORD-FAIL-01")
                .customerId(10L)
                .amount(new BigDecimal("999999.00")) // Trigger decline
                .paymentMethod("CREDIT_CARD")
                .build();

        PaymentTransaction savedTx = PaymentTransaction.builder()
                .id(2L)
                .paymentReference("PAY-DECLINED-REF")
                .orderNumber("ORD-FAIL-01")
                .customerId(10L)
                .amount(new BigDecimal("999999.00"))
                .paymentMethod("CREDIT_CARD")
                .paymentStatus(PaymentStatus.FAILED)
                .failureReason("Card declined: Insufficient funds or invalid security code")
                .processedAt(Instant.now())
                .build();

        when(repository.save(any(PaymentTransaction.class))).thenReturn(savedTx);

        PaymentResponse response = paymentService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertNotNull(response.getFailureReason());

        verify(kafkaTemplate, times(1)).send(eq("test-payment-processed-topic"), eq("ORD-FAIL-01"), any());
    }

    @Test
    @DisplayName("Should retrieve payment by orderNumber")
    void shouldGetPaymentByOrderNumber() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentReference("PAY-123")
                .orderNumber("ORD-555")
                .customerId(10L)
                .amount(new BigDecimal("75.00"))
                .paymentMethod("DEBIT_CARD")
                .paymentStatus(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        when(repository.findByOrderNumber("ORD-555")).thenReturn(Optional.of(tx));

        PaymentResponse response = paymentService.getPaymentByOrderNumber("ORD-555");
        assertNotNull(response);
        assertEquals("PAY-123", response.getPaymentReference());
    }
}

