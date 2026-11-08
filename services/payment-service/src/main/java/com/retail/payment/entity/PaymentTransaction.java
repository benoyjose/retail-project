package com.retail.payment.entity;

import com.retail.common.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_order", columnList = "order_number"),
        @Index(name = "idx_payment_reference", columnList = "payment_reference")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String paymentReference;

    @Column(nullable = false, length = 50)
    private String orderNumber;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus paymentStatus;

    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false)
    private Instant processedAt;

    @PrePersist
    public void prePersist() {
        if (this.processedAt == null) {
            this.processedAt = Instant.now();
        }
    }
}

