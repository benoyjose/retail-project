package com.retail.payment.repository;

import com.retail.common.enums.PaymentStatus;
import com.retail.payment.entity.PaymentTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PaymentTransactionRepositoryTest {

    @Autowired
    private PaymentTransactionRepository repository;

    @Test
    @DisplayName("Should persist and retrieve payment transaction by orderNumber")
    void shouldPersistAndFindByOrderNumber() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentReference("PAY-REF-12345")
                .orderNumber("ORD-1001")
                .customerId(10L)
                .amount(new BigDecimal("299.99"))
                .paymentMethod("CREDIT_CARD")
                .paymentStatus(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        repository.save(tx);

        Optional<PaymentTransaction> found = repository.findByOrderNumber("ORD-1001");
        assertTrue(found.isPresent());
        assertEquals("PAY-REF-12345", found.get().getPaymentReference());
        assertEquals(PaymentStatus.SUCCESS, found.get().getPaymentStatus());
        assertEquals(new BigDecimal("299.99"), found.get().getAmount());
    }

    @Test
    @DisplayName("Should find payment transaction by paymentReference")
    void shouldFindByPaymentReference() {
        PaymentTransaction tx = PaymentTransaction.builder()
                .paymentReference("PAY-REF-UNIQUE")
                .orderNumber("ORD-1002")
                .customerId(20L)
                .amount(new BigDecimal("50.00"))
                .paymentMethod("PAYPAL")
                .paymentStatus(PaymentStatus.SUCCESS)
                .processedAt(Instant.now())
                .build();

        repository.save(tx);

        Optional<PaymentTransaction> found = repository.findByPaymentReference("PAY-REF-UNIQUE");
        assertTrue(found.isPresent());
        assertEquals("ORD-1002", found.get().getOrderNumber());
    }
}

